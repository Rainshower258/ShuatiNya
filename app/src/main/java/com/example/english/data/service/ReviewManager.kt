package com.example.english.data.service

import com.example.english.util.DateTimeHelper

/**
 * 复习管理器
 * 管理艾宾浩斯复习间隔和复习逻辑
 */
object ReviewManager {

    /**
     * 复习时间常量 - 明确语义
     * M-1 修复: 使用显式常量代替魔法数字 0L
     */
    object ReviewTimeConstants {
        const val NOT_LEARNED = 0L          // 未学习
        const val FULLY_MASTERED = -1L      // 已完全掌握（不再需要复习）
        const val ERROR_STATE = -2L         // 错误状态
    }

    // 复习间隔配置（天数）
    private val REVIEW_INTERVALS = intArrayOf(
        1,   // 第1次复习: 1天后
        3,   // 第2次复习: 3天后
        7,   // 第3次复习: 7天后
        15,  // 第4次复习: 15天后
        30   // 第5次复习: 30天后
    )

    /**
     * 获取复习阶段的间隔天数
     * @param stage 复习阶段 (1-5)
     * @return 间隔天数
     */
    fun getIntervalDays(stage: Int): Int {
        if (stage <= 0) return 0
        val index = (stage - 1).coerceIn(0, REVIEW_INTERVALS.size - 1)
        return REVIEW_INTERVALS[index]
    }

    /**
     * 计算下次复习时间
     * @param firstLearnDate 首次学习时间
     * @param stage 当前复习阶段
     * @return 下次复习时间戳
     */
    fun calculateNextReviewTime(firstLearnDate: Long, stage: Int): Long {
        if (stage <= 0) return ReviewTimeConstants.NOT_LEARNED
        val intervalDays = getIntervalDays(stage)
        return DateTimeHelper.addDays(firstLearnDate, intervalDays)
    }

    /**
     * 判断单词是否需要复习
     * @param nextReviewTime 下次复习时间
     * @return true=需要复习, false=还不需要
     */
    fun needsReview(nextReviewTime: Long): Boolean {
        // 未学习或已完全掌握，不需要复习
        if (nextReviewTime == ReviewTimeConstants.NOT_LEARNED ||
            nextReviewTime == ReviewTimeConstants.FULLY_MASTERED) {
            return false
        }
        val now = DateTimeHelper.getCurrentTimeMillis()
        return now >= nextReviewTime
    }

    /**
     * 单词记住后，进入下一个复习阶段
     * @param currentStage 当前阶段
     * @return 新的复习阶段
     */
    fun advanceStage(currentStage: Int): Int {
        val maxStage = REVIEW_INTERVALS.size
        return (currentStage + 1).coerceAtMost(maxStage)
    }

    /**
     * 单词忘记后，重置到第一阶段
     * @return 重置后的阶段
     */
    fun resetStage(): Int {
        return 1
    }

    /**
     * 获取阶段描述
     */
    fun getStageDescription(stage: Int): String {
        return when (stage) {
            0 -> "未学习"
            1 -> "1天后复习"
            2 -> "3天后复习"
            3 -> "7天后复习"
            4 -> "15天后复习"
            5 -> "30天后复习"
            else -> "已掌握"
        }
    }

    /**
     * 初始化学习记录（首次学习时调用）
     * @return Pair<reviewStage, nextReviewTime>
     */
    fun initializeReview(): Pair<Int, Long> {
        val stage = 1
        val firstLearnDate = DateTimeHelper.getCurrentTimeMillis()
        val nextReviewTime = calculateNextReviewTime(firstLearnDate, stage)
        return Pair(stage, nextReviewTime)
    }

    /**
     * 更新复习记录（记住）
     * @param firstLearnDate 首次学习日期
     * @param currentStage 当前阶段
     * @return Pair<newStage, nextReviewTime>
     */
    fun updateReviewRemembered(firstLearnDate: Long, currentStage: Int): Pair<Int, Long> {
        val newStage = advanceStage(currentStage)
        val nextReviewTime = if (newStage <= REVIEW_INTERVALS.size) {
            calculateNextReviewTime(firstLearnDate, newStage)
        } else {
            ReviewTimeConstants.FULLY_MASTERED // M-1: 已完全掌握，不再需要复习
        }
        return Pair(newStage, nextReviewTime)
    }

    /**
     * 更新复习记录（忘记）
     * @param firstLearnDate 首次学习日期
     * @return Pair<newStage, nextReviewTime>
     */
    fun updateReviewForgotten(firstLearnDate: Long): Pair<Int, Long> {
        val newStage = resetStage()
        val nextReviewTime = calculateNextReviewTime(firstLearnDate, newStage)
        return Pair(newStage, nextReviewTime)
    }
}

