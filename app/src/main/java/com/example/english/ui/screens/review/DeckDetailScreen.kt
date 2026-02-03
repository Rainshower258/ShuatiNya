package com.example.english.ui.screens.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.english.data.model.DeckStudyRecord
import com.example.english.data.repository.ReviewRepository

/**
 * 词库详情界面 - 显示单个词库的统计和学习记录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: Long,
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

    val viewModel: DeckDetailViewModel = viewModel(
        factory = DeckDetailViewModelFactory(deckId, reviewRepository)
    )

    val deckReviewInfo by viewModel.deckReviewInfo.collectAsState()
    val studyRecords by viewModel.studyRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deckReviewInfo?.deckName ?: "词库详情") },
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                deckReviewInfo == null -> {
                    DeckDetailErrorView(
                        message = "加载失败",
                        onRetry = { viewModel.refresh() }
                    )
                }
                else -> {
                    DeckDetailContent(
                        deckReviewInfo = deckReviewInfo!!,
                        studyRecords = studyRecords,
                        onDeleteRecord = { viewModel.showDeleteConfirmation(it) },
                        onStartReview = {
                            navController.navigate("review_practice/$deckId")
                        }
                    )
                }
            }
        }
    }

    // 删除确认对话框
    showDeleteDialog?.let { record: DeckStudyRecord ->
        DeleteConfirmDialog(
            record = record,
            onConfirm = { viewModel.deleteRecord(record) },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }
}

@Composable
fun DeckDetailContent(
    deckReviewInfo: DeckReviewInfo,
    studyRecords: List<DeckStudyRecord>,
    onDeleteRecord: (DeckStudyRecord) -> Unit,
    onStartReview: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            TodayGoalCard(
                reviewWords = deckReviewInfo.reviewWords,
                onStartReview = onStartReview
            )
        }

        item {
            LearningStatisticsCard(deckReviewInfo = deckReviewInfo)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("学习记录", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${studyRecords.size} 条记录", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (studyRecords.isEmpty()) {
            item { EmptyRecordsView() }
        } else {
            items(studyRecords) { record ->
                StudyRecordCard(record = record, onDelete = { onDeleteRecord(record) })
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun TodayGoalCard(reviewWords: Int, onStartReview: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (reviewWords > 0) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("📚 今日目标", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (reviewWords > 0) "有 $reviewWords 个单词等待复习" else "今日暂无需要复习的单词",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (reviewWords > 0) {
                    Button(onClick = onStartReview) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("开始复习")
                    }
                }
            }
        }
    }
}

@Composable
fun LearningStatisticsCard(deckReviewInfo: DeckReviewInfo) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("学习统计", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatisticColumn("总词数", deckReviewInfo.totalWords.toString(), MaterialTheme.colorScheme.onSurface)
                StatisticColumn("已学习", deckReviewInfo.learnedWords.toString(), MaterialTheme.colorScheme.primary)
                StatisticColumn("待复习", deckReviewInfo.reviewWords.toString(), MaterialTheme.colorScheme.error)
                StatisticColumn("已掌握", deckReviewInfo.masteredWords.toString(), MaterialTheme.colorScheme.tertiary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatisticColumn("今日学习", deckReviewInfo.todayLearned.toString(), MaterialTheme.colorScheme.secondary)
                StatisticColumn("今日复习", deckReviewInfo.todayReviewed.toString(), MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun StatisticColumn(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StudyRecordCard(record: DeckStudyRecord, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.date, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("完成 ${record.completedCount}/${record.plannedCount} 个", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "正确率 ${record.accuracyPercent}%",
                    fontSize = 13.sp,
                    color = if (record.accuracyPercent >= 80) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun EmptyRecordsView() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📝", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("暂无学习记录", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("开始学习后，这里将显示你的学习历史", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DeckDetailErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("❌", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("重试") }
    }
}

@Composable
fun DeleteConfirmDialog(record: DeckStudyRecord, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("确认删除") },
        text = { Text("确定要删除 ${record.date} 的学习记录吗？此操作不可恢复。") },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("删除")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

