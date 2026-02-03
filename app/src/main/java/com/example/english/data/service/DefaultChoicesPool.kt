/*
 * MIT License
 * Copyright (c) 2025 sun6 (Rainshower258)
 * See LICENSE file in the project root for full license information.
 */
package com.example.english.data.service

import com.example.english.data.model.ChoiceOption

/**
 * 固定选项池 - 用于词库单词不足时的备用选项
 * 包含50个常用单词作为干扰项
 */
object DefaultChoicesPool {

    val defaultChoices = listOf(
        ChoiceOption("学习", "/ˈstʌdi/", "v.", false),
        ChoiceOption("工作", "/wɜːrk/", "n.", false),
        ChoiceOption("时间", "/taɪm/", "n.", false),
        ChoiceOption("people", "/ˈpiːpl/", "n.", false),
        ChoiceOption("方式", "/weɪ/", "n.", false),
        ChoiceOption("say", "/seɪ/", "v.", false),
        ChoiceOption("好的", "/ɡʊd/", "adj.", false),
        ChoiceOption("新的", "/njuː/", "adj.", false),
        ChoiceOption("第一", "/fɜːrst/", "adj.", false),
        ChoiceOption("使用", "/juːz/", "v.", false),
        ChoiceOption("找到", "/faɪnd/", "v.", false),
        ChoiceOption("给予", "/ɡɪv/", "v.", false),
        ChoiceOption("告诉", "/tel/", "v.", false),
        ChoiceOption("成为", "/bɪˈkʌm/", "v.", false),
        ChoiceOption("离开", "/liːv/", "v.", false),
        ChoiceOption("感觉", "/fiːl/", "v.", false),
        ChoiceOption("尝试", "/traɪ/", "v.", false),
        ChoiceOption("ask", "/ɑːsk/", "v.", false),
        ChoiceOption("need", "/niːd/", "v.", false),
        ChoiceOption("看起来", "/lʊk/", "v.", false),
        ChoiceOption("想要", "/wɒnt/", "v.", false),
        ChoiceOption("come", "/kʌm/", "v.", false),
        ChoiceOption("制作", "/meɪk/", "v.", false),
        ChoiceOption("知道", "/nəʊ/", "v.", false),
        ChoiceOption("得到", "/ɡet/", "v.", false),
        ChoiceOption("看见", "/siː/", "v.", false),
        ChoiceOption("认为", "/θɪŋk/", "v.", false),
        ChoiceOption("go", "/ɡəʊ/", "v.", false),
        ChoiceOption("take", "/teɪk/", "v.", false),
        ChoiceOption("能够", "/kæn/", "v.", false),
        ChoiceOption("大的", "/bɪɡ/", "adj.", false),
        ChoiceOption("小的", "/smɔːl/", "adj.", false),
        ChoiceOption("重要的", "/ɪmˈpɔːtnt/", "adj.", false),
        ChoiceOption("年轻的", "/jʌŋ/", "adj.", false),
        ChoiceOption("几个", "/ˈsevərəl/", "adj.", false),
        ChoiceOption("公共的", "/ˈpʌblɪk/", "adj.", false),
        ChoiceOption("长的", "/lɒŋ/", "adj.", false),
        ChoiceOption("伟大的", "/ɡreɪt/", "adj.", false),
        ChoiceOption("相同的", "/seɪm/", "adj.", false),
        ChoiceOption("不同的", "/ˈdɪfrənt/", "adj.", false),
        ChoiceOption("高的", "/haɪ/", "adj.", false),
        ChoiceOption("国家", "/ˈkʌntri/", "n.", false),
        ChoiceOption("生活", "/laɪf/", "n.", false),
        ChoiceOption("世界", "/wɜːrld/", "n.", false),
        ChoiceOption("学校", "/skuːl/", "n.", false),
        ChoiceOption("家庭", "/ˈfæməli/", "n.", false),
        ChoiceOption("问题", "/ˈprɒbləm/", "n.", false),
        ChoiceOption("事实", "/fækt/", "n.", false),
        ChoiceOption("情况", "/ˈkeɪs/", "n.", false),
        ChoiceOption("地方", "/pleɪs/", "n.", false)
    )

    /**
     * 获取随机的默认选项
     * @param count 需要的选项数量
     * @param excludeChinese 要排除的中文（避免与正确答案相同）
     */
    fun getRandomDefaults(count: Int, excludeChinese: String): List<ChoiceOption> {
        return defaultChoices
            .filter { it.text != excludeChinese }
            .shuffled()
            .take(count)
    }
}
