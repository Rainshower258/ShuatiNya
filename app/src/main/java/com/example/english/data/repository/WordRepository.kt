package com.example.english.data.repository

import com.example.english.data.database.dao.WordDao
import com.example.english.data.database.entity.WordEntity
import com.example.english.data.database.entity.toWord
import com.example.english.data.database.entity.toEntity
import com.example.english.data.model.Word
import com.example.english.data.model.ImportResult
import com.example.english.data.parser.WordParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class WordRepository(
    private val wordDao: WordDao,
    private val wordParser: WordParser = WordParser()
) {

    /**
     * 获取指定词库的所有单词
     */
    fun getWordsByDeckId(deckId: Long): Flow<List<Word>> {
        return wordDao.getWordsByDeckId(deckId).map { entities ->
            entities.map { it.toWord() }
        }
    }

    /**
     * 获取指定词库的随机单词
     */
    suspend fun getRandomWordsFromDeck(deckId: Long, count: Int): List<Word> {
        return wordDao.getRandomWordsFromDeck(deckId, count).map { it.toWord() }
    }

    /**
     * 获取指定词库的困难单词（错误次数多于正确次数）
     */
    suspend fun getDifficultWordsFromDeck(deckId: Long): List<Word> {
        return wordDao.getDifficultWordsFromDeck(deckId).map { it.toWord() }
    }

    /**
     * 获取需要复习的单词
     */
    suspend fun getWordsForReview(): List<Word> {
        val currentTime = System.currentTimeMillis()
        return wordDao.getWordsForReview(currentTime).map { it.toWord() }
    }

    /**
     * 获取指定词库的单词数量
     */
    suspend fun getWordCountByDeckId(deckId: Long): Int {
        return wordDao.getWordCountByDeckId(deckId)
    }

    /**
     * 获取已学习的单词（仅英文和中文）
     */
    suspend fun getStudiedWords(deckId: Long): List<Word> {
        return wordDao.getStudiedWords(deckId).map { it.toWord() }
    }

    /**
     * 根据ID获取单词
     */
    suspend fun getWordById(id: Long): Word? {
        return wordDao.getWordById(id)?.toWord()
    }

    /**
     * 添加单词
     */
    suspend fun insertWord(word: Word): Long {
        return wordDao.insertWord(word.toEntity())
    }

    /**
     * 批量添加单词
     */
    suspend fun insertWords(words: List<Word>): List<Long> {
        return wordDao.insertWords(words.map { it.toEntity() })
    }

    /**
     * 更新单词
     */
    suspend fun updateWord(word: Word) {
        wordDao.updateWord(word.toEntity())
    }

    /**
     * 删除单词
     */
    suspend fun deleteWord(word: Word) {
        wordDao.deleteWord(word.toEntity())
    }

    /**
     * 删除指定词库的所有单词
     */
    suspend fun deleteWordsByDeckId(deckId: Long) {
        wordDao.deleteWordsByDeckId(deckId)
    }

    /**
     * 导入文本格式的单词
     */
    suspend fun importWordsFromText(text: String, deckId: Long): ImportResult {
        // 优先尝试标记格式解析（支持空行）
        val markedWords = parseTextToWordsWithMarkers(text, deckId)
        if (markedWords.isNotEmpty()) {
            val (uniqueWords, duplicateCount) = deduplicateWords(markedWords, deckId)
            return if (uniqueWords.isNotEmpty()) {
                insertWords(uniqueWords)
                ImportResult(
                    successCount = uniqueWords.size,
                    failureCount = 0,
                    duplicateCount = duplicateCount
                )
            } else {
                ImportResult(
                    successCount = 0,
                    failureCount = 0,
                    duplicateCount = duplicateCount,
                    errors = listOf("所有单词都已存在（重复）")
                )
            }
        }

        // 回退到原有格式解析（无标记）
        val words = parseTextToWords(text, deckId)

        if (words.isEmpty()) {
            return ImportResult(0, 0, 0, listOf("没有找到有效的单词"))
        }

        // 去重处理：按英文+词性判断
        val (uniqueWords, duplicateCount) = deduplicateWords(words, deckId)

        return if (uniqueWords.isNotEmpty()) {
            insertWords(uniqueWords)
            ImportResult(
                successCount = uniqueWords.size,
                failureCount = 0,
                duplicateCount = duplicateCount
            )
        } else {
            ImportResult(
                successCount = 0,
                failureCount = 0,
                duplicateCount = duplicateCount,
                errors = listOf("所有单词都已存在（重复）")
            )
        }
    }

    /**
     * 使用标记解析文本（支持空行）
     * 标记格式：<W> 和 </W> 作为词条边界标记
     */
    private fun parseTextToWordsWithMarkers(text: String, deckId: Long): List<Word> {
        val words = mutableListOf<Word>()

        // 使用正则表达式提取 <W> 和 </W> 之间的内容
        val pattern = """<W>(.*?)</W>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val matches = pattern.findAll(text)

        for (match in matches) {
            val block = match.groupValues[1].trim()
            if (block.isNotBlank()) {
                val word = parseWordBlock(block, deckId)
                if (word != null) {
                    words.add(word)
                }
            }
        }

        return words
    }

    /**
     * 解析单个词条块
     */
    private fun parseWordBlock(block: String, deckId: Long): Word? {
        val data = mutableMapOf<String, String>()
        val lines = block.lines()

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            when {
                trimmedLine.startsWith("英文：") -> {
                    data["english"] = trimmedLine.removePrefix("英文：").trim()
                }
                trimmedLine.startsWith("中文对照：") || trimmedLine.startsWith("中文：") -> {
                    val prefix = if (trimmedLine.startsWith("中文对照：")) "中文对照：" else "中文："
                    data["chinese"] = trimmedLine.removePrefix(prefix).trim()
                }
                trimmedLine.startsWith("词性：") -> {
                    data["pos"] = trimmedLine.removePrefix("词性：").trim()
                }
                trimmedLine.startsWith("音标：") -> {
                    data["phonetic"] = trimmedLine.removePrefix("音标：").trim()
                }
                trimmedLine.startsWith("类型：") -> {
                    data["type"] = trimmedLine.removePrefix("类型：").trim()
                }
                trimmedLine.startsWith("用法：") || trimmedLine.startsWith("用法示例：") -> {
                    val prefix = if (trimmedLine.startsWith("用法示例：")) "用法示例：" else "用法："
                    data["usage"] = trimmedLine.removePrefix(prefix).trim()
                }
            }
        }

        return createWordFromData(data, deckId)
    }

    /**
     * 去重：按英文+词性判断是否重复
     * 返回：(去重后的单词列表, 重复数量)
     */
    private suspend fun deduplicateWords(words: List<Word>, deckId: Long): Pair<List<Word>, Int> {
        val uniqueWords = mutableListOf<Word>()
        var duplicateCount = 0

        for (word in words) {
            // 检查数据库中是否已存在相同英文和词性的单词
            val existingWord = wordDao.findDuplicateWord(deckId, word.english, word.partOfSpeech)

            if (existingWord == null) {
                // 不存在，添加到唯一列表
                uniqueWords.add(word)
            } else {
                // 已存在，计数
                duplicateCount++
            }
        }

        return Pair(uniqueWords, duplicateCount)
    }

    /**
     * 导入文件格式的单词
     */
    suspend fun importWordsFromFile(file: File, deckId: Long): ImportResult {
        val words = parseFileToWords(file, deckId)

        if (words.isEmpty()) {
            return ImportResult(0, 0, 0, listOf("文件中没有找到有效的单词"))
        }

        // 去重处理：按英文+词性判断
        val (uniqueWords, duplicateCount) = deduplicateWords(words, deckId)

        return if (uniqueWords.isNotEmpty()) {
            insertWords(uniqueWords)
            ImportResult(
                successCount = uniqueWords.size,
                failureCount = 0,
                duplicateCount = duplicateCount
            )
        } else {
            ImportResult(
                successCount = 0,
                failureCount = 0,
                duplicateCount = duplicateCount,
                errors = listOf("所有单词都已存在（重复）")
            )
        }
    }

    /**
     * 辅助方法：将文本解析为单词列表（支持短语）
     */
    private fun parseTextToWords(text: String, deckId: Long): List<Word> {
        val words = mutableListOf<Word>()
        val lines = text.split("\n")
        var currentWordData = mutableMapOf<String, String>()

        for (line in lines) {
            val trimmedLine = line.trim()

            if (trimmedLine.isEmpty()) {
                if (currentWordData.isNotEmpty()) {
                    createWordFromData(currentWordData, deckId)?.let { words.add(it) }
                    currentWordData.clear()
                }
                continue
            }

            when {
                trimmedLine.startsWith("英文：") -> {
                    currentWordData["english"] = trimmedLine.removePrefix("英文：").trim()
                }
                trimmedLine.startsWith("中文对照：") || trimmedLine.startsWith("中文：") -> {
                    val prefix = if (trimmedLine.startsWith("中文对照：")) "中文对照：" else "中文："
                    currentWordData["chinese"] = trimmedLine.removePrefix(prefix).trim()
                }
                trimmedLine.startsWith("词性：") -> {
                    currentWordData["pos"] = trimmedLine.removePrefix("词性：").trim()
                }
                trimmedLine.startsWith("音标：") -> {
                    currentWordData["phonetic"] = trimmedLine.removePrefix("音标：").trim()
                }
                trimmedLine.startsWith("类型：") -> {
                    currentWordData["type"] = trimmedLine.removePrefix("类型：").trim()
                }
                trimmedLine.startsWith("用法：") || trimmedLine.startsWith("用法示例：") -> {
                    val prefix = if (trimmedLine.startsWith("用法示例：")) "用法示例：" else "用法："
                    currentWordData["usage"] = trimmedLine.removePrefix(prefix).trim()
                }
            }
        }

        // 处理最后一个单词
        if (currentWordData.isNotEmpty()) {
            createWordFromData(currentWordData, deckId)?.let { words.add(it) }
        }

        return words
    }

    /**
     * 辅助方法：将文件解析为单词列表
     */
    private fun parseFileToWords(file: File, deckId: Long): List<Word> {
        return when (file.extension.lowercase()) {
            "txt" -> parseTextToWords(file.readText(), deckId)
            "csv" -> parseCsvToWords(file, deckId)
            "tsv" -> parseTsvToWords(file, deckId)
            else -> emptyList()
        }
    }

    private fun parseCsvToWords(file: File, deckId: Long): List<Word> {
        val words = mutableListOf<Word>()
        file.forEachLine { line ->
            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.size >= 4) {
                val word = Word(
                    english = parts[0],
                    chinese = parts[1],
                    partOfSpeech = parts[2],
                    phonetic = parts[3],
                    deckId = deckId
                )
                words.add(word)
            }
        }
        return words
    }

    private fun parseTsvToWords(file: File, deckId: Long): List<Word> {
        val words = mutableListOf<Word>()
        file.forEachLine { line ->
            val parts = line.split("\t").map { it.trim() }
            if (parts.size >= 4) {
                val word = Word(
                    english = parts[0],
                    chinese = parts[1],
                    partOfSpeech = parts[2],
                    phonetic = parts[3],
                    deckId = deckId
                )
                words.add(word)
            }
        }
        return words
    }

    private fun createWordFromData(data: Map<String, String>, deckId: Long): Word? {
        val english = data["english"]
        val chinese = data["chinese"]
        val pos = data["pos"]
        val phonetic = data["phonetic"] ?: "" // 短语可能没有音标
        val type = data["type"]?.lowercase() // "word" 或 "phrase"
        val usage = data["usage"] // 短语用法示例

        // 必需字段检查
        return if (!english.isNullOrBlank() && !chinese.isNullOrBlank() && !pos.isNullOrBlank()) {
            val wordType = when (type) {
                "phrase", "短语" -> com.example.english.data.model.WordType.PHRASE
                else -> com.example.english.data.model.WordType.WORD
            }

            Word(
                english = english,
                chinese = chinese,
                partOfSpeech = pos,
                phonetic = phonetic,
                deckId = deckId,
                wordType = wordType,
                phraseUsage = if (wordType == com.example.english.data.model.WordType.PHRASE) usage else null
            )
        } else null
    }

    /**
     * 搜索单词
     */
    suspend fun searchWords(deckId: Long, keyword: String): List<Word> {
        return wordDao.searchWords(deckId, keyword).map { it.toWord() }
    }

    /**
     * 删除多个单词
     */
    suspend fun deleteWords(words: List<Word>) {
        words.forEach { word ->
            wordDao.deleteWord(word.toEntity())
        }
    }
}

