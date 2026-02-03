package com.example.english.data

import com.example.english.data.model.Word
import com.example.english.data.model.WordType

/**
 * 初始短语数据
 * 包含50个常用英语短语
 */
object InitialPhrases {

    fun getPhrases(deckId: Long): List<Word> {
        return listOf(
            Word(
                english = "break down",
                chinese = "分解；出故障；崩溃",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "The car broke down on the highway."
            ),
            Word(
                english = "carry out",
                chinese = "执行；实施；完成",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "We need to carry out the plan carefully."
            ),
            Word(
                english = "come across",
                chinese = "偶然遇见；偶然发现",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "I came across an old friend yesterday."
            ),
            Word(
                english = "deal with",
                chinese = "处理；对付；论述",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "How do you deal with stress at work?"
            ),
            Word(
                english = "figure out",
                chinese = "弄清楚；计算出；解决",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "I can't figure out how to solve this problem."
            ),
            Word(
                english = "get along",
                chinese = "相处；进展；离开",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Do you get along well with your colleagues?"
            ),
            Word(
                english = "give up",
                chinese = "放弃；投降；戒除",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Don't give up on your dreams."
            ),
            Word(
                english = "look after",
                chinese = "照顾；照料；关心",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "She looks after her elderly parents."
            ),
            Word(
                english = "make up",
                chinese = "组成；编造；化妆；和解",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Women make up 52% of the population."
            ),
            Word(
                english = "put off",
                chinese = "推迟；延期；使反感",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "We had to put off the meeting until next week."
            ),
            Word(
                english = "run into",
                chinese = "偶然遇见；遭遇；撞上",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "I ran into some difficulties with the project."
            ),
            Word(
                english = "take off",
                chinese = "起飞；脱下；成功；休假",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "The plane will take off in ten minutes."
            ),
            Word(
                english = "turn down",
                chinese = "拒绝；调小；关小",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "I had to turn down their offer."
            ),
            Word(
                english = "work out",
                chinese = "计算出；锻炼；解决；成功",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "I work out at the gym three times a week."
            ),
            Word(
                english = "look forward to",
                chinese = "期待；盼望",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "I'm looking forward to your reply."
            ),
            Word(
                english = "in terms of",
                chinese = "就...而言；在...方面",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "In terms of quality, this product is excellent."
            ),
            Word(
                english = "on behalf of",
                chinese = "代表；为了",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "I'm speaking on behalf of the entire team."
            ),
            Word(
                english = "at the expense of",
                chinese = "以...为代价；由...支付",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "He achieved success at the expense of his health."
            ),
            Word(
                english = "in spite of",
                chinese = "尽管；不管；不顾",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "In spite of the rain, we went hiking."
            ),
            Word(
                english = "by means of",
                chinese = "通过...方式；借助于",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "We communicate by means of email."
            ),
            Word(
                english = "for the sake of",
                chinese = "为了...起见；为了...的利益",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "For the sake of clarity, let me explain again."
            ),
            Word(
                english = "in accordance with",
                chinese = "依照；根据；与...一致",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Act in accordance with the rules."
            ),
            Word(
                english = "with regard to",
                chinese = "关于；至于",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "With regard to your question, here's my answer."
            ),
            Word(
                english = "as far as",
                chinese = "就...而言；远至；直到",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "As far as I know, he's still in Beijing."
            ),
            Word(
                english = "in addition to",
                chinese = "除...之外（还）",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "In addition to English, she speaks French."
            ),
            Word(
                english = "due to",
                chinese = "由于；因为；应归于",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "The delay was due to heavy traffic."
            ),
            Word(
                english = "apart from",
                chinese = "除了...之外；此外",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Apart from being fun, it's also educational."
            ),
            Word(
                english = "prior to",
                chinese = "在...之前；先于",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Prior to joining the company, I was a teacher."
            ),
            Word(
                english = "in contrast to",
                chinese = "与...对比；与...相反",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "In contrast to last year, sales have increased."
            ),
            Word(
                english = "regardless of",
                chinese = "不管；不顾；无论",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Everyone is equal regardless of race or gender."
            ),
            Word(
                english = "according to",
                chinese = "根据；按照；取决于",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "According to the report, sales are up."
            ),
            Word(
                english = "instead of",
                chinese = "代替；而不是",
                partOfSpeech = "介词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "I'll have tea instead of coffee."
            ),
            Word(
                english = "in order to",
                chinese = "为了；以便",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Study hard in order to pass the exam."
            ),
            Word(
                english = "as well as",
                chinese = "也；和；不但...而且",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "She speaks French as well as English."
            ),
            Word(
                english = "such as",
                chinese = "例如；比如；诸如",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Fruits such as apples and oranges are healthy."
            ),
            Word(
                english = "so that",
                chinese = "以便；为了；结果是",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Speak clearly so that everyone can hear you."
            ),
            Word(
                english = "even though",
                chinese = "即使；尽管；虽然",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Even though it's raining, we'll go out."
            ),
            Word(
                english = "in case",
                chinese = "以防；万一；假使",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Take an umbrella in case it rains."
            ),
            Word(
                english = "as long as",
                chinese = "只要；如果；既然",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "You can stay as long as you want."
            ),
            Word(
                english = "as soon as",
                chinese = "一...就；尽快",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Call me as soon as you arrive."
            ),
            Word(
                english = "not only...but also",
                chinese = "不仅...而且；既...又",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "She's not only smart but also hardworking."
            ),
            Word(
                english = "either...or",
                chinese = "要么...要么；或者...或者",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "You can either stay or leave."
            ),
            Word(
                english = "neither...nor",
                chinese = "既不...也不；两者都不",
                partOfSpeech = "连词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "He is neither rich nor famous."
            ),
            Word(
                english = "on the other hand",
                chinese = "另一方面；反之",
                partOfSpeech = "副词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "The job pays well. On the other hand, it's stressful."
            ),
            Word(
                english = "in other words",
                chinese = "换句话说；也就是说",
                partOfSpeech = "副词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "In other words, we need to work harder."
            ),
            Word(
                english = "for example",
                chinese = "例如；比如",
                partOfSpeech = "副词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "Many countries, for example China, are developing rapidly."
            ),
            Word(
                english = "in fact",
                chinese = "事实上；实际上",
                partOfSpeech = "副词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "In fact, I've never been there before."
            ),
            Word(
                english = "as a result",
                chinese = "结果；因此",
                partOfSpeech = "副词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "He didn't study. As a result, he failed the exam."
            ),
            Word(
                english = "in conclusion",
                chinese = "总之；最后",
                partOfSpeech = "副词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "In conclusion, I'd like to thank everyone for coming."
            ),
            Word(
                english = "to sum up",
                chinese = "总结；概括地说",
                partOfSpeech = "动词短语",
                phonetic = "",
                deckId = deckId,
                wordType = WordType.PHRASE,
                phraseUsage = "To sum up, we need more time and resources."
            )
        )
    }
}

