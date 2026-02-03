package com.example.english.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.english.ui.screens.deck_list.DeckListScreen
import com.example.english.ui.screens.import_words.ImportWordsScreen
import com.example.english.ui.screens.import_questions.ImportQuestionsScreen
import com.example.english.ui.screens.study.StudySetupScreen
import com.example.english.ui.screens.study.StudyScreen
import com.example.english.ui.screens.studied_words.StudiedWordsScreen
import com.example.english.ui.screens.practiced_questions.PracticedQuestionsScreen
import com.example.english.ui.screens.SettingsScreen
import com.example.english.ui.screens.review.ReviewScreen
import com.example.english.ui.screens.review.ReviewPracticeScreen
import com.example.english.ui.screens.review.ReviewCenterScreen
import com.example.english.ui.screens.review.DeckDetailScreen
import com.example.english.ui.screens.question_practice.QuestionSetupScreen
import com.example.english.ui.screens.question_practice.QuestionPracticeScreen
import com.example.english.ui.screens.view_words.ViewWordsScreen
import com.example.english.ui.screens.view_questions.ViewQuestionsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "deck_list") {
        composable("deck_list") {
            DeckListScreen(navController = navController)
        }

        composable(
            "import_words/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            ImportWordsScreen(navController = navController, deckId = deckId)
        }

        composable(
            "import_questions/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            ImportQuestionsScreen(navController = navController, deckId = deckId)
        }

        composable(
            "study_setup/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            StudySetupScreen(navController = navController, deckId = deckId)
        }

        composable(
            "study/{deckId}/{plannedCount}",
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("plannedCount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            val plannedCount = backStackEntry.arguments?.getInt("plannedCount") ?: 10
            StudyScreen(
                navController = navController,
                deckId = deckId,
                plannedCount = plannedCount
            )
        }

        composable(
            "studied_words/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            StudiedWordsScreen(navController = navController, deckId = deckId)
        }

        composable(
            "practiced_questions/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            PracticedQuestionsScreen(navController = navController, deckId = deckId)
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // 复习中心（新版 - 按词库分组）
        composable("review") {
            ReviewCenterScreen(navController = navController)
        }

        // 词库详情（复习中心）
        composable(
            "deck_detail/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            DeckDetailScreen(deckId = deckId, navController = navController)
        }

        // 刷题设置（选择题目数量）
        composable(
            "question_setup/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            QuestionSetupScreen(navController = navController, deckId = deckId)
        }

        // 刷题界面
        composable(
            "question_practice/{deckId}/{plannedCount}",
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("plannedCount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            val plannedCount = backStackEntry.arguments?.getInt("plannedCount") ?: 10
            QuestionPracticeScreen(
                navController = navController,
                deckId = deckId,
                plannedCount = plannedCount
            )
        }

        // 复习练习（全局）
        composable("review_practice") {
            ReviewPracticeScreen(navController = navController)
        }

        // 复习练习（指定词库）
        composable(
            "review_practice/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            // TODO: 传递 deckId 到 ReviewPracticeScreen，让它只复习该词库的单词
            ReviewPracticeScreen(navController = navController)
        }

        // 旧的复习界面（保留作为备用）
        composable("review_old") {
            ReviewScreen(navController = navController)
        }

        // 复习详情（按日期查看 - 旧版）
        composable(
            "review_detail/{date}",
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            // TODO: 实现复习详情界面
            ReviewScreen(navController = navController)
        }

        // 查看单词列表
        composable(
            "view_words/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            ViewWordsScreen(
                deckId = deckId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // 查看题目列表
        composable(
            "view_questions/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            ViewQuestionsScreen(
                deckId = deckId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

