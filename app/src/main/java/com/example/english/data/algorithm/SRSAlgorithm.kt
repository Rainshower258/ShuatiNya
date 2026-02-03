package com.example.english.data.algorithm

import com.example.english.util.DateTimeHelper
import kotlin.math.roundToInt

/**
 * 间隔重复系统（Spaced Repetition System）算法
 * 基于艾宾浩斯遗忘曲线实现
 */
class SRSAlgorithm {

    companion object {
        // 默认参数
        private const val INITIAL_EASE_FACTOR = 2.5f
        private const val MINIMUM_EASE_FACTOR = 1.3f
        private const val INITIAL_INTERVAL = 1
        private const val SECOND_INTERVAL = 6

        // M-4: 最大间隔上限（天数）- 防止整数溢出
        private const val MAX_INTERVAL_DAYS = 365 * 3  // 最大3年

        // 质量评分
        const val QUALITY_PERFECT = 5 // 完美回答
        const val QUALITY_CORRECT = 4 // 正确但有些犹豫
        const val QUALITY_CORRECT_HARD = 3 // 正确但很困难
        const val QUALITY_WRONG_EASY = 2 // 错误但想起来了
        const val QUALITY_WRONG = 1 // 完全错误
        const val QUALITY_BLACKOUT = 0 // 完全不记得
    }

    /**
     * 计算下次复习的参数
     */
    data class ReviewResult(
        val nextInterval: Int, // 下次复习间隔（天）
        val easeFactor: Float, // 新的难度因子
        val repetition: Int, // 重复次数
        val nextReviewTime: Long // 下次复习时间戳
    )

    /**
     * 根据回答质量计算下次复习时间
     *
     * @param quality 回答质量 (0-5)
     * @param currentEaseFactor 当前难度因子
     * @param currentInterval 当前间隔
     * @param repetition 当前重复次数
     * @return 复习结果
     */
    fun calculateNextReview(
        quality: Int,
        currentEaseFactor: Float = INITIAL_EASE_FACTOR,
        currentInterval: Int = INITIAL_INTERVAL,
        repetition: Int = 0
    ): ReviewResult {
        // M-2: 输入验证 - 防止非法 quality 值破坏算法
        require(quality in QUALITY_BLACKOUT..QUALITY_PERFECT) {
            "Quality must be in range 0-5 (QUALITY_BLACKOUT to QUALITY_PERFECT), got $quality"
        }
        require(currentEaseFactor >= MINIMUM_EASE_FACTOR) {
            "Ease factor must be >= $MINIMUM_EASE_FACTOR, got $currentEaseFactor"
        }
        require(currentInterval > 0) {
            "Interval must be positive, got $currentInterval"
        }
        require(repetition >= 0) {
            "Repetition must be non-negative, got $repetition"
        }

        val newEaseFactor = calculateNewEaseFactor(quality, currentEaseFactor)

        val (newRepetition, newInterval) = when {
            quality < 3 -> {
                // 回答质量差，重新开始
                0 to INITIAL_INTERVAL
            }
            repetition == 0 -> {
                // 第一次复习
                1 to INITIAL_INTERVAL
            }
            repetition == 1 -> {
                // 第二次复习
                2 to SECOND_INTERVAL
            }
            else -> {
                // 后续复习
                // M-4: 限制最大间隔，防止整数溢出
                val interval = (currentInterval * newEaseFactor).roundToInt()
                    .coerceIn(1, MAX_INTERVAL_DAYS)
                (repetition + 1) to interval
            }
        }

        // M-3: 使用 DateTimeHelper.addDays() 正确处理 DST（夏令时）
        val nextReviewTime = DateTimeHelper.addDays(System.currentTimeMillis(), newInterval)

        return ReviewResult(
            nextInterval = newInterval,
            easeFactor = newEaseFactor,
            repetition = newRepetition,
            nextReviewTime = nextReviewTime
        )
    }

    /**
     * 根据回答质量计算新的难度因子
     */
    private fun calculateNewEaseFactor(quality: Int, currentEaseFactor: Float): Float {
        val newEaseFactor = currentEaseFactor + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))
        return maxOf(newEaseFactor, MINIMUM_EASE_FACTOR)
    }

    /**
     * 根据用户选择结果确定质量分数
     */
    fun getQualityFromUserChoice(isCorrect: Boolean, attemptCount: Int): Int {
        return when {
            isCorrect && attemptCount == 1 -> QUALITY_PERFECT // 一次正确
            isCorrect && attemptCount == 2 -> QUALITY_CORRECT // 两次正确
            isCorrect && attemptCount > 2 -> QUALITY_CORRECT_HARD // 多次尝试才正确
            !isCorrect -> QUALITY_WRONG // 错误
            else -> QUALITY_WRONG
        }
    }

    /**
     * 检查是否需要复习
     */
    fun shouldReview(nextReviewTime: Long): Boolean {
        return System.currentTimeMillis() >= nextReviewTime
    }

    /**
     * 获取距离下次复习的天数
     */
    fun getDaysUntilNextReview(nextReviewTime: Long): Int {
        val diff = nextReviewTime - System.currentTimeMillis()
        return (diff / (24 * 60 * 60 * 1000L)).toInt()
    }
}
