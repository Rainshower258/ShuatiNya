package com.example.english.ui.screens.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.english.data.model.ReviewRecord
import com.example.english.util.DateTimeHelper

/**
 * 复习主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    navController: NavController
) {
    val application = LocalContext.current.applicationContext as EnglishApp
    val database = application.database

    val viewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModelFactory(database.wordDao())
    )

    val reviewRecords by viewModel.reviewRecords.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("复习中心") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 统计卡片
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    statistics?.let { stats ->
                        StatisticsCard(statistics = stats)
                    }
                }

                // 今日复习按钮
                item {
                    val todayReviewCount = statistics?.totalToReview ?: 0
                    TodayReviewCard(
                        reviewCount = todayReviewCount,
                        onClick = {
                            if (todayReviewCount > 0) {
                                navController.navigate("review_practice")
                            }
                        }
                    )
                }

                // 学习记录标题
                item {
                    Text(
                        text = "学习记录",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 按日期显示记录
                items(reviewRecords) { record ->
                    ReviewRecordCard(
                        record = record,
                        onClick = {
                            navController.navigate("review_detail/${record.date}")
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * 统计卡片
 */
@Composable
private fun StatisticsCard(statistics: com.example.english.data.model.ReviewStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "学习统计",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "已学习", value = statistics.totalLearned.toString())
                StatItem(label = "待复习", value = statistics.totalToReview.toString())
                StatItem(label = "已掌握", value = statistics.masteredWords.toString())
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "今日学习", value = statistics.todayLearned.toString())
                StatItem(label = "今日复习", value = statistics.todayReviewed.toString())
                StatItem(label = "连续天数", value = "${statistics.continuousDays}天")
            }
        }
    }
}

/**
 * 统计项
 */
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

/**
 * 今日复习卡片
 */
@Composable
private fun TodayReviewCard(
    reviewCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = reviewCount > 0, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (reviewCount > 0)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📚 今日复习",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (reviewCount > 0)
                        "有 $reviewCount 个单词等待复习"
                    else
                        "暂无需要复习的单词",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (reviewCount > 0) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "开始复习",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 复习记录卡片
 */
@Composable
private fun ReviewRecordCard(
    record: ReviewRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = DateTimeHelper.getFriendlyDate(record.dateTimestamp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = record.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "学习 ${record.totalWordsLearned} 个",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                if (record.totalWordsToReview > 0) {
                    Text(
                        text = "待复习 ${record.totalWordsToReview} 个",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

