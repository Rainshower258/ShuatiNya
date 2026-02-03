package com.example.english.data.parser

import com.example.english.data.model.Question
import com.example.english.data.model.QuestionType

/**
 * 题目解析器
 * 支持解析包含以下格式的文本（不含解析字段）
 */
class QuestionParser {

    companion object {
        // 支持的格式提示信息
        val SUPPORTED_FORMATS = """
支持的题目导入格式：

【标记格式】每个题目必须用 <Q> 和 </Q> 包裹：

1. 单选题格式：
<Q>
题型：单选
题目：What is the capital of France?
选项A：London
选项B：Paris
选项C：Berlin
选项D：Madrid
正确答案：B
</Q>

2. 多选题格式：
<Q>
题型：多选
题目：Which of the following are programming languages?
选项A：Python
选项B：HTML
选项C：Java
选项D：CSS
正确答案：AC
</Q>

3. 判断题格式：
<Q>
题型：判断
题目：The Earth is flat.
正确答案：false
</Q>

【注意事项】
- <Q> 和 </Q> 必须独占一行
- 题型必须是：单选、多选、判断
- 单选和多选题必须提供选项A和B，选项C和D可选
- 判断题的答案为：true/false 或 正确/错误
- 多选题的正确答案用字母连写，如：AC、ABD
- 支持中文内容，自动处理UTF-8编码
        """.trimIndent()

        /**
         * 获取AI Prompt（用于复制）
         */
        fun getAIPrompt(): String {
            return """
请生成题目并按照以下格式输出，每个题目必须用 <Q> 和 </Q> 标记包裹。

【单选题格式】
<Q>
题型：单选
题目：[题目内容]
选项A：[选项内容]
选项B：[选项内容]
选项C：[选项内容]
选项D：[选项内容]
正确答案：[A/B/C/D]
</Q>

【多选题格式】
<Q>
题型：多选
题目：[题目内容]
选项A：[选项内容]
选项B：[选项内容]
选项C：[选项内容]
选项D：[选项内容]
正确答案：[如AC、BD等]
</Q>

【判断题格式】
<Q>
题型：判断
题目：[题目内容]
正确答案：[true/false]
</Q>

注意：
1. <Q>和</Q>必须独占一行
2. 字段间不要有空行
3. 题型必须是：单选、多选、判断
4. 多选题答案用字母连写（如AC）
5. 判断题答案用true或false
            """.trimIndent()
        }
    }

    /**
     * 解析文本为题目列表
     */
    fun parseText(text: String, deckId: Long): List<Question> {
        val questions = mutableListOf<Question>()

        // 使用标记格式解析
        val pattern = """<Q>(.*?)</Q>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val matches = pattern.findAll(text)

        for (match in matches) {
            val block = match.groupValues[1].trim()
            val question = parseQuestionBlock(block, deckId)
            if (question != null) {
                questions.add(question)
            }
        }

        return questions
    }

    /**
     * 解析单个题目块
     */
    private fun parseQuestionBlock(block: String, deckId: Long): Question? {
        try {
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            val dataMap = mutableMapOf<String, String>()

            for (line in lines) {
                when {
                    line.startsWith("题型：") -> dataMap["type"] = line.substringAfter("题型：").trim()
                    line.startsWith("题目：") -> dataMap["question"] = line.substringAfter("题目：").trim()
                    line.startsWith("选项A：") -> dataMap["optionA"] = line.substringAfter("选项A：").trim()
                    line.startsWith("选项B：") -> dataMap["optionB"] = line.substringAfter("选项B：").trim()
                    line.startsWith("选项C：") -> dataMap["optionC"] = line.substringAfter("选项C：").trim()
                    line.startsWith("选项D：") -> dataMap["optionD"] = line.substringAfter("选项D：").trim()
                    line.startsWith("正确答案：") -> dataMap["answer"] = line.substringAfter("正确答案：").trim()
                }
            }

            val typeStr = dataMap["type"] ?: return null
            val questionText = dataMap["question"] ?: return null
            val answer = dataMap["answer"] ?: return null

            val questionType = when (typeStr) {
                "单选", "single" -> QuestionType.SINGLE_CHOICE
                "多选", "multiple" -> QuestionType.MULTIPLE_CHOICE
                "判断", "truefalse", "true/false" -> QuestionType.TRUE_FALSE
                else -> return null
            }

            return Question(
                deckId = deckId,
                questionType = questionType,
                questionText = questionText,
                optionA = dataMap["optionA"],
                optionB = dataMap["optionB"],
                optionC = dataMap["optionC"],
                optionD = dataMap["optionD"],
                correctAnswer = answer
            )
        } catch (e: Exception) {
            return null
        }
    }
}

