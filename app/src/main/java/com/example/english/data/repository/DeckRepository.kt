package com.example.english.data.repository

import com.example.english.data.database.dao.DeckDao
import com.example.english.data.database.dao.WordDao
import com.example.english.data.database.dao.QuestionDao
import com.example.english.data.database.entity.DeckEntity
import com.example.english.data.database.entity.toDeck
import com.example.english.data.database.entity.toEntity
import com.example.english.data.model.Deck
import com.example.english.data.model.DeckType
import com.example.english.data.model.StudyStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeckRepository(
    private val deckDao: DeckDao,
    private val wordDao: WordDao,
    private val questionDao: QuestionDao? = null
) {

    /**
     * 获取所有活跃的词库
     */
    fun getAllActiveDecks(): Flow<List<Deck>> {
        return deckDao.getAllActiveDecks().map { entities ->
            entities.map { it.toDeck() }
        }
    }

    /**
     * 获取所有词库（包括非活跃的）
     */
    fun getAllDecks(): Flow<List<Deck>> {
        return deckDao.getAllDecks().map { entities ->
            entities.map { it.toDeck() }
        }
    }

    /**
     * 根据ID获取词库
     */
    suspend fun getDeckById(id: Long): Deck? {
        return deckDao.getDeckById(id)?.toDeck()
    }

    /**
     * 创建新词库
     */
    suspend fun createDeck(name: String, description: String = "", deckType: com.example.english.data.model.DeckType = com.example.english.data.model.DeckType.VOCABULARY): Long {
        val deck = DeckEntity(
            name = name,
            description = description,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deckType = deckType.name
        )
        return deckDao.insertDeck(deck)
    }

    /**
     * 更新词库信息
     */
    suspend fun updateDeck(deck: Deck) {
        val entity = deck.copy(updatedAt = System.currentTimeMillis()).toEntity()
        deckDao.updateDeck(entity)
    }

    /**
     * 删除词库（软删除，设为非活跃状态）
     */
    suspend fun deleteDeck(deckId: Long) {
        deckDao.deactivateDeck(deckId)
    }

    /**
     * 彻底删除词库（硬删除）
     */
    suspend fun permanentlyDeleteDeck(deck: Deck) {
        // 根据词库类型删除对应的内容
        if (deck.deckType == DeckType.QUESTION) {
            // 删除题库中的所有题目
            questionDao?.deleteQuestionsByDeckId(deck.id)
        } else {
            // 删除词库中的所有单词
            wordDao.deleteWordsByDeckId(deck.id)
        }
        // 删除词库本身
        deckDao.deleteDeck(deck.toEntity())
    }

    /**
     * 更新词库的单词数量
     */
    suspend fun updateDeckWordCount(deckId: Long) {
        val wordCount = wordDao.getWordCountByDeckId(deckId)
        deckDao.updateWordCount(deckId, wordCount, System.currentTimeMillis())
    }

    /**
     * 获取词库的学习统计信息
     */
    suspend fun getDeckStudyStats(deckId: Long): StudyStats {
        val totalWords = wordDao.getWordCountByDeckId(deckId)
        val wordEntities = wordDao.getRandomWordsFromDeck(deckId, totalWords) // 获取所有单词来计算统计

        val studiedWords = wordEntities.count { it.lastReviewTime > 0 }
        val masteredWords = wordEntities.count {
            it.correctCount > 0 && it.correctCount >= it.wrongCount * 2 // 简单的掌握定义
        }

        // 今日学习数量（简化实现）
        val today = System.currentTimeMillis()
        val todayStart = today - (today % (24 * 60 * 60 * 1000))
        val todayStudiedCount = wordEntities.count { it.lastReviewTime >= todayStart }

        return StudyStats(
            deckId = deckId,
            totalWords = totalWords,
            studiedWords = studiedWords,
            masteredWords = masteredWords,
            todayStudiedCount = todayStudiedCount,
            studyStreak = 0 // 连续学习天数的计算需要更复杂的逻辑
        )
    }

    /**
     * 导出词库为文本格式
     */
    suspend fun exportDeckToText(deckId: Long): String {
        val words = wordDao.getRandomWordsFromDeck(deckId, Int.MAX_VALUE) // 获取所有单词
        val stringBuilder = StringBuilder()

        words.forEach { word ->
            stringBuilder.appendLine("英文：${word.english}")
            stringBuilder.appendLine("中文对照：${word.chinese}")
            stringBuilder.appendLine("词性：${word.partOfSpeech}")
            stringBuilder.appendLine("音标：${word.phonetic}")
            stringBuilder.appendLine() // 空行分隔
        }

        return stringBuilder.toString()
    }

    /**
     * 导出词库为CSV格式
     */
    suspend fun exportDeckToCsv(deckId: Long): String {
        val words = wordDao.getRandomWordsFromDeck(deckId, Int.MAX_VALUE)
        val stringBuilder = StringBuilder()

        // CSV标题行
        stringBuilder.appendLine("English,Chinese,PartOfSpeech,Phonetic")

        words.forEach { word ->
            stringBuilder.appendLine("\"${word.english}\",\"${word.chinese}\",\"${word.partOfSpeech}\",\"${word.phonetic}\"")
        }

        return stringBuilder.toString()
    }
}

