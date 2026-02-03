package com.example.english.data.parser

import com.example.english.data.model.Word
import com.example.english.data.model.ImportResult
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

/**
 * 词汇解析器
 * 支持解析包含以下格式的文本：
 * 英文：word
 * 中文对照：单词
 * 词性：n.
 * 音标：/wɜːrd/
 */
class WordParser {

    companion object {
        private const val ENGLISH_PREFIX = "英文："
        private const val CHINESE_PREFIX = "中文对照："
        private const val POS_PREFIX = "词性："
        private const val PHONETIC_PREFIX = "音标："

        // 支持的文件格式提示信息
        val SUPPORTED_FORMATS = """
支持的导入格式：

1. 推荐格式（使用标记，支持空行）：

<W>
英文：hello
中文对照：你好
词性：interj.
音标：/həˈloʊ/
类型：word
</W>

<W>
英文：world
中文对照：世界
词性：n.
音标：/wɜːrld/
类型：word
</W>

<W>
英文：break down
中文对照：分解；出故障
词性：动词短语
类型：phrase
用法：The car broke down on the highway.
</W>

2. 传统格式（向后兼容，要求字段连续无空行）：

英文：in terms of
中文对照：就...而言
词性：介词短语
类型：phrase
用法：In terms of quality, this product is excellent.

注意：
- 推荐使用 <W></W> 标记格式，容错性更强
- 标记格式中，<W> 和 </W> 必须独占一行
- 短语的"音标"字段可省略
- "类型"字段：word（单词）或 phrase（短语）
- 短语需要提供"用法"示例句

3. 支持的文件格式：
- TXT文件 (.txt) - 纯文本格式
- CSV文件 (.csv) - 逗号分隔值格式
- TSV文件 (.tsv) - 制表符分隔值格式

4. AI 识别提示词（支持单词+短语）：

请从以下图片或文档中提取英语单词和常用短语，并转换为标准格式。

【提取要求】
1. 单词：提取核心词汇、专业术语
2. 短语：提取固定搭配、习惯用语、常用表达（2-5个单词）

【格式要求】
每个词条必须用 <W> 和 </W> 标记包裹，每个字段独立一行：

单词格式（字段间无空行）：
<W>
英文：[英文单词]
中文对照：[中文翻译，最多2个意思，用顿号分隔]
词性：[词性，如n./v./adj./adv.等]
音标：[国际音标，用/包围]
类型：word
</W>

短语格式（字段间无空行）：
<W>
英文：[英语短语]
中文对照：[中文翻译]
词性：[短语类型，如动词短语、介词短语等]
类型：phrase
用法：[英文用法示例句]
</W>

【重要规则】
1. 每个词条必须用 <W> 开始，</W> 结束（标记独占一行）
2. 每个词条的所有字段必须连续，字段之间不能有空行
3. 不同词条之间可以有空行（标记外的空行会被忽略）
4. 中文对照最多提供2个主要意思，用顿号（、）分隔
5. 严格按照"字段名：内容"的格式
6. 短语的释义要体现其整体含义
7. 短语优先提取：动词短语、介词短语、固定搭配
8. 避免提取过于简单的短语（如"in the"）

【正确示例】
<W>
英文：comprehensive
中文对照：全面的、综合的
词性：adj.
音标：/ˌkɒmprɪˈhensɪv/
类型：word
</W>

<W>
英文：carry out
中文对照：执行、实施
词性：动词短语
类型：phrase
用法：They carried out the experiment successfully.
</W>

<W>
英文：break down
中文对照：分解、拆分
词性：动词短语
类型：phrase
用法：Let's break down this problem.
</W>

请确保每个词条都用 <W></W> 标记包裹，标记独占一行，同一词条内字段连续无空行！

【常见短语类型参考】
动词短语：break down, carry out, give up, look after, put off
介词短语：in terms of, on behalf of, in spite of, due to
连词短语：in order to, as well as, so that, even though
副词短语：on the other hand, for example, in fact, as a result
        """.trimIndent()

        /**
         * 获取用于复制的 AI Prompt 格式
         * 用户可以复制此内容发送给 AI
         */
        fun getAIPrompt(): String = """
请从以下图片或文档中提取英语单词和常用短语，并转换为标准格式。

【提取要求】
1. 单词：提取核心词汇、专业术语
2. 短语：提取固定搭配、习惯用语、常用表达（2-5个单词）

【格式要求】
每个词条必须用 <W> 和 </W> 标记包裹，每个字段独立一行：

单词格式（字段间无空行）：
<W>
英文：[英文单词]
中文对照：[中文翻译，最多2个意思，用顿号分隔]
词性：[词性，如n./v./adj./adv.等]
音标：[国际音标，用/包围]
类型：word
</W>

短语格式（字段间无空行）：
<W>
英文：[英语短语]
中文对照：[中文翻译]
词性：[短语类型，如动词短语、介词短语等]
类型：phrase
用法：[英文用法示例句]
</W>

【重要规则】
1. 每个词条必须用 <W> 开始，</W> 结束（标记独占一行）
2. 每个词条的所有字段必须连续，字段之间不能有空行
3. 不同词条之间可以有空行（标记外的空行会被忽略）
4. 中文对照最多提供2个主要意思，用顿号（、）分隔
5. 严格按照"字段名：内容"的格式
6. 短语的释义要体现其整体含义
7. 短语优先提取：动词短语、介词短语、固定搭配
8. 避免提取过于简单的短语（如"in the"）

【正确示例】
<W>
英文：comprehensive
中文对照：全面的、综合的
词性：adj.
音标：/ˌkɒmprɪˈhensɪv/
类型：word
</W>

<W>
英文：carry out
中文对照：执行、实施
词性：动词短语
类型：phrase
用法：They carried out the experiment successfully.
</W>

<W>
英文：break down
中文对照：分解、拆分
词性：动词短语
类型：phrase
用法：Let's break down this problem.
</W>

请确保每个词条都用 <W></W> 标记包裹，标记独占一行，同一词条内字段连续无空行！

【常见短语类型参考】
动词短语：break down, carry out, give up, look after, put off
介词短语：in terms of, on behalf of, in spite of, due to
连词短语：in order to, as well as, so that, even though
副词短语：on the other hand, for example, in fact, as a result
        """.trimIndent()
    }

    /**
     * 解析粘贴的文本内容
     */
    fun parseText(text: String, deckId: Long): ImportResult {
        val words = mutableListOf<Word>()
        val errors = mutableListOf<String>()

        val lines = text.split("\n")
        var currentWordData = mutableMapOf<String, String>()
        var lineNumber = 0

        for (line in lines) {
            lineNumber++
            val trimmedLine = line.trim()

            if (trimmedLine.isEmpty()) {
                // 空行表示一个单词结束
                if (currentWordData.isNotEmpty()) {
                    val result = createWordFromData(currentWordData, deckId, lineNumber)
                    if (result.isSuccess) {
                        words.add(result.getOrThrow())
                    } else {
                        errors.add("第${lineNumber}行: ${result.exceptionOrNull()?.message}")
                    }
                    currentWordData.clear()
                }
                continue
            }

            when {
                trimmedLine.startsWith(ENGLISH_PREFIX) -> {
                    currentWordData["english"] = trimmedLine.removePrefix(ENGLISH_PREFIX).trim()
                }
                trimmedLine.startsWith(CHINESE_PREFIX) -> {
                    currentWordData["chinese"] = trimmedLine.removePrefix(CHINESE_PREFIX).trim()
                }
                trimmedLine.startsWith(POS_PREFIX) -> {
                    currentWordData["pos"] = trimmedLine.removePrefix(POS_PREFIX).trim()
                }
                trimmedLine.startsWith(PHONETIC_PREFIX) -> {
                    currentWordData["phonetic"] = trimmedLine.removePrefix(PHONETIC_PREFIX).trim()
                }
            }
        }

        // 处理最后一个单词（如果文本末尾没有空行）
        if (currentWordData.isNotEmpty()) {
            val result = createWordFromData(currentWordData, deckId, lineNumber)
            if (result.isSuccess) {
                words.add(result.getOrThrow())
            } else {
                errors.add("第${lineNumber}行: ${result.exceptionOrNull()?.message}")
            }
        }

        return ImportResult(
            successCount = words.size,
            failureCount = errors.size,
            errors = errors
        )
    }

    /**
     * 解析文件内容
     */
    fun parseFile(file: File, deckId: Long): ImportResult {
        return try {
            when (file.extension.lowercase()) {
                "txt" -> parseTextFile(file, deckId)
                "csv" -> parseCsvFile(file, deckId)
                "tsv" -> parseTsvFile(file, deckId)
                else -> ImportResult(0, 1, 0, listOf("不支持的文件格式: ${file.extension}"))
            }
        } catch (e: Exception) {
            ImportResult(0, 1, 0, listOf("文件解析错误: ${e.message}"))
        }
    }

    private fun parseTextFile(file: File, deckId: Long): ImportResult {
        val content = file.readText()
        return parseText(content, deckId)
    }

    private fun parseCsvFile(file: File, deckId: Long): ImportResult {
        val words = mutableListOf<Word>()
        val errors = mutableListOf<String>()

        BufferedReader(FileReader(file)).use { reader ->
            var lineNumber = 0
            reader.forEachLine { line ->
                lineNumber++
                if (lineNumber == 1) return@forEachLine // 跳过标题行

                val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
                if (parts.size >= 4) {
                    try {
                        val word = Word(
                            english = parts[0],
                            chinese = parts[1],
                            partOfSpeech = parts[2],
                            phonetic = parts[3],
                            deckId = deckId
                        )
                        words.add(word)
                    } catch (e: Exception) {
                        errors.add("第${lineNumber}行: ${e.message}")
                    }
                } else {
                    errors.add("第${lineNumber}行: 数据格式不完整")
                }
            }
        }

        return ImportResult(words.size, errors.size, 0, errors)
    }

    private fun parseTsvFile(file: File, deckId: Long): ImportResult {
        val words = mutableListOf<Word>()
        val errors = mutableListOf<String>()

        BufferedReader(FileReader(file)).use { reader ->
            var lineNumber = 0
            reader.forEachLine { line ->
                lineNumber++
                if (lineNumber == 1) return@forEachLine // 跳过标题行

                val parts = line.split("\t").map { it.trim() }
                if (parts.size >= 4) {
                    try {
                        val word = Word(
                            english = parts[0],
                            chinese = parts[1],
                            partOfSpeech = parts[2],
                            phonetic = parts[3],
                            deckId = deckId
                        )
                        words.add(word)
                    } catch (e: Exception) {
                        errors.add("第${lineNumber}行: ${e.message}")
                    }
                } else {
                    errors.add("第${lineNumber}行: 数据格式不完整")
                }
            }
        }

        return ImportResult(words.size, errors.size, 0, errors)
    }

    private fun createWordFromData(data: Map<String, String>, deckId: Long, lineNumber: Int): Result<Word> {
        val english = data["english"]
        val chinese = data["chinese"]
        val pos = data["pos"]
        val phonetic = data["phonetic"]

        return when {
            english.isNullOrBlank() -> Result.failure(Exception("缺少英文单词"))
            chinese.isNullOrBlank() -> Result.failure(Exception("缺少中文对照"))
            pos.isNullOrBlank() -> Result.failure(Exception("缺少词性"))
            phonetic.isNullOrBlank() -> Result.failure(Exception("缺少音标"))
            else -> Result.success(
                Word(
                    english = english,
                    chinese = chinese,
                    partOfSpeech = pos,
                    phonetic = phonetic,
                    deckId = deckId
                )
            )
        }
    }
}

