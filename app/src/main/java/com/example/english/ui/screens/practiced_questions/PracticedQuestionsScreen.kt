package com.example.english.ui.screens.practiced_questions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.english.EnglishApp
import com.example.english.data.model.Question
import com.example.english.data.model.QuestionType
import com.example.english.data.repository.QuestionRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticedQuestionsScreen(
    navController: NavController,
    deckId: Long
) {
    val application = LocalContext.current.applicationContext as EnglishApp

    // 使用 remember 缓存 Repository，避免重组时重复创建
    val questionRepository = remember {
        val database = application.database
        QuestionRepository(database.questionDao())
    }

    val viewModel: PracticedQuestionsViewModel = viewModel(
        factory = remember { PracticedQuestionsViewModelFactory(questionRepository) }
    )

    val questions by viewModel.questions.collectAsState()
    val stats by viewModel.stats.collectAsState()

    LaunchedEffect(deckId) {
        viewModel.loadQuestions(deckId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("已刷题目") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 统计卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("总题数", stats.totalQuestions.toString())
                    StatItem("已练习", stats.learnedQuestions.toString())
                    StatItem("待复习", stats.reviewQuestions.toString())
                    StatItem("已掌握", stats.masteredQuestions.toString())
                }
            }

            // 题目列表
            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无已刷题目",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(questions) { question ->
                        QuestionCard(question = question)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuestionCard(question: Question) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFF2C2C2C) // 深色背景
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 题型标签
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when (question.questionType) {
                            QuestionType.SINGLE_CHOICE -> "单选"
                            QuestionType.MULTIPLE_CHOICE -> "多选"
                            QuestionType.TRUE_FALSE -> "判断"
                        },
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                // 正确率
                val accuracy = if (question.correctCount + question.wrongCount > 0) {
                    (question.correctCount.toFloat() / (question.correctCount + question.wrongCount) * 100).toInt()
                } else {
                    0
                }
                Text(
                    text = "正确率: $accuracy%",
                    fontSize = 12.sp,
                    color = if (accuracy >= 80)
                        MaterialTheme.colorScheme.primary
                    else if (accuracy >= 60)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 题目内容（截断显示）
            Text(
                text = question.questionText,
                fontSize = 14.sp,
                maxLines = 2,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "✓ ${question.correctCount}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "✗ ${question.wrongCount}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "练习 ${question.correctCount + question.wrongCount} 次",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

