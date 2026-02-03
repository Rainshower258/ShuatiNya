package com.example.english.ui.screens.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import com.example.english.data.model.DeckReviewInfo
import com.example.english.data.repository.ReviewRepository
import java.text.SimpleDateFormat
import java.util.*

/**
 * 复习中心主界面 - 显示词库列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewCenterScreen(
    navController: NavController
) {
    val application = LocalContext.current.applicationContext as EnglishApp
    val database = application.database

    val reviewRepository = remember {
        ReviewRepository(
            deckDao = database.deckDao(),
            wordDao = database.wordDao(),
            studySessionDao = database.studySessionDao()
        )
    }

    val viewModel: ReviewCenterViewModel = viewModel(
        factory = ReviewCenterViewModelFactory(reviewRepository)
    )

    val deckReviewList by viewModel.deckReviewList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("复习中心") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    ErrorView(
                        message = errorMessage ?: "未知错误",
                        onRetry = { viewModel.refresh() }
                    )
                }
                deckReviewList.isEmpty() -> {
                    EmptyView(
                        message = "暂无词库数据",
                        description = "请先导入词库后再使用复习功能"
                    )
                }
                else -> {
                    DeckListContent(
                        deckList = deckReviewList,
                        onDeckClick = { deckId ->
                            navController.navigate("deck_detail/$deckId")
                        }
                    )
                }
            }
        }
    }
}

/**
 * 词库列表内容
 */
@Composable
private fun DeckListContent(
    deckList: List<DeckReviewInfo>,
    onDeckClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "选择词库进行复习",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(deckList) { deckInfo ->
            DeckReviewCard(
                deckInfo = deckInfo,
                onClick = { onDeckClick(deckInfo.deckId) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 词库复习卡片
 */
@Composable
private fun DeckReviewCard(
    deckInfo: DeckReviewInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 词库名称
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📖",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = deckInfo.deckName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 统计数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    label = "总词数",
                    value = deckInfo.totalWords.toString(),
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatisticItem(
                    label = "已学习",
                    value = deckInfo.learnedWords.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                StatisticItem(
                    label = "待复习",
                    value = deckInfo.reviewWords.toString(),
                    color = if (deckInfo.reviewWords > 0)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatisticItem(
                    label = "已掌握",
                    value = deckInfo.masteredWords.toString(),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // 今日学习情况
            if (deckInfo.todayLearned > 0 || deckInfo.todayReviewed > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = "今日学习 ${deckInfo.todayLearned} 个",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "今日复习 ${deckInfo.todayReviewed} 个",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // 最后学习时间
            deckInfo.lastStudyDate?.let { timestamp ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "最后学习：${formatLastStudyTime(timestamp)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatisticItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 空状态视图
 */
@Composable
private fun EmptyView(
    message: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📚",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 错误视图
 */
@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

/**
 * 格式化最后学习时间
 */
private fun formatLastStudyTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)

    return when {
        minutes < 60 -> "${minutes}分钟前"
        hours < 24 -> "${hours}小时前"
        days < 7 -> "${days}天前"
        else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}

