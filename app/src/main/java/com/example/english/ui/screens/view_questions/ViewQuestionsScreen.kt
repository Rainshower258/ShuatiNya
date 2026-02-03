package com.example.english.ui.screens.view_questions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.english.EnglishApp
import com.example.english.data.model.Question
import com.example.english.data.model.QuestionType
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.QuestionRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewQuestionsScreen(
    deckId: Long,
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as EnglishApp

    // 使用 remember 缓存 Repository，避免重组时重复创建
    val (questionRepository, deckRepository) = remember {
        val database = application.database
        Pair(
            QuestionRepository(database.questionDao()),
            DeckRepository(database.deckDao(), database.wordDao())
        )
    }

    val viewModel: ViewQuestionsViewModel = viewModel(
        factory = remember { ViewQuestionsViewModelFactory(questionRepository, deckRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<Question?>(null) }

    LaunchedEffect(deckId) {
        viewModel.loadQuestions(deckId)
    }

    // 显示消息
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        if (uiState.errorMessage != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.deckName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (uiState.selectedQuestions.isNotEmpty()) {
                        Text(
                            text = "已选 ${uiState.selectedQuestions.size}",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconButton(onClick = { viewModel.selectAllQuestions() }) {
                            Icon(Icons.Default.CheckCircle, "全选")
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Clear, "取消选择")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                        }
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
            // 搜索框
            OutlinedTextField(
                value = uiState.searchKeyword,
                onValueChange = { viewModel.onSearchKeywordChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索题目...") },
                leadingIcon = { Icon(Icons.Default.Search, "搜索") },
                trailingIcon = {
                    if (uiState.searchKeyword.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchKeywordChange("") }) {
                            Icon(Icons.Default.Clear, "清除")
                        }
                    }
                },
                singleLine = true
            )

            // 消息提示
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            uiState.successMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 题目列表
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredQuestions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchKeyword.isBlank()) "暂无题目" else "未找到匹配的题目",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredQuestions, key = { it.id }) { question ->
                        QuestionItem(
                            question = question,
                            isSelected = question.id in uiState.selectedQuestions,
                            onToggleSelection = { viewModel.toggleQuestionSelection(question.id) },
                            onEdit = {
                                editingQuestion = question
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除选中的 ${uiState.selectedQuestions.size} 个题目吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedQuestions()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 编辑题目对话框
    if (showEditDialog && editingQuestion != null) {
        EditQuestionDialog(
            question = editingQuestion!!,
            onDismiss = {
                showEditDialog = false
                editingQuestion = null
            },
            onConfirm = { updatedQuestion ->
                viewModel.updateQuestion(updatedQuestion)
                showEditDialog = false
                editingQuestion = null
            }
        )
    }
}

@Composable
fun QuestionItem(
    question: Question,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelection),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                Color(0xFF2C2C2C) // 深色背景
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // 题型标签和编辑按钮
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Surface(
                            color = when (question.questionType) {
                                QuestionType.SINGLE_CHOICE -> Color(0xFF4CAF50)
                                QuestionType.MULTIPLE_CHOICE -> Color(0xFF2196F3)
                                QuestionType.TRUE_FALSE -> Color(0xFFFF9800)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when (question.questionType) {
                                    QuestionType.SINGLE_CHOICE -> "单选"
                                    QuestionType.MULTIPLE_CHOICE -> "多选"
                                    QuestionType.TRUE_FALSE -> "判断"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // 编辑按钮
                        if (!isSelected) {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "编辑",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // 题目文本（支持滚动）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 选项
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOfNotNull(
                            question.optionA?.let { "A" to it },
                            question.optionB?.let { "B" to it },
                            question.optionC?.let { "C" to it },
                            question.optionD?.let { "D" to it }
                        ).forEach { (label, text) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "$label.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (question.correctAnswer.contains(label))
                                        Color(0xFF4CAF50)
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (question.correctAnswer.contains(label))
                                        Color(0xFF4CAF50)
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 正确答案
                    Text(
                        text = "正确答案: ${question.correctAnswer}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    // 学习统计
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "正确: ${question.correctCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "错误: ${question.wrongCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF44336)
                        )
                        if (question.reviewStage > 0) {
                            Text(
                                text = "复习阶段: ${question.reviewStage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 选择框（始终可见）
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .padding(start = 8.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "已选中",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ),
                            color = Color.Transparent
                        ) {}
                    }
                }
            }
        }
    }
}

// 编辑题目对话框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionDialog(
    question: Question,
    onDismiss: () -> Unit,
    onConfirm: (Question) -> Unit
) {
    var questionText by remember { mutableStateOf(question.questionText) }
    var questionType by remember { mutableStateOf(question.questionType) }
    var optionA by remember { mutableStateOf(question.optionA ?: "") }
    var optionB by remember { mutableStateOf(question.optionB ?: "") }
    var optionC by remember { mutableStateOf(question.optionC ?: "") }
    var optionD by remember { mutableStateOf(question.optionD ?: "") }
    var correctAnswer by remember { mutableStateOf(question.correctAnswer) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑题目") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 题型选择
                Text("题型", fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = questionType == QuestionType.SINGLE_CHOICE,
                        onClick = { questionType = QuestionType.SINGLE_CHOICE },
                        label = { Text("单选") }
                    )
                    FilterChip(
                        selected = questionType == QuestionType.MULTIPLE_CHOICE,
                        onClick = { questionType = QuestionType.MULTIPLE_CHOICE },
                        label = { Text("多选") }
                    )
                    FilterChip(
                        selected = questionType == QuestionType.TRUE_FALSE,
                        onClick = { questionType = QuestionType.TRUE_FALSE },
                        label = { Text("判断") }
                    )
                }

                // 题目文本
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("题目") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // 选项（判断题不显示）
                if (questionType != QuestionType.TRUE_FALSE) {
                    OutlinedTextField(
                        value = optionA,
                        onValueChange = { optionA = it },
                        label = { Text("选项 A") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = optionB,
                        onValueChange = { optionB = it },
                        label = { Text("选项 B") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = optionC,
                        onValueChange = { optionC = it },
                        label = { Text("选项 C（可选）") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = optionD,
                        onValueChange = { optionD = it },
                        label = { Text("选项 D（可选）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 正确答案
                OutlinedTextField(
                    value = correctAnswer,
                    onValueChange = { correctAnswer = it },
                    label = {
                        Text(
                            if (questionType == QuestionType.TRUE_FALSE)
                                "正确答案 (true/false)"
                            else
                                "正确答案 (如: A 或 AC)"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank() && correctAnswer.isNotBlank()) {
                        onConfirm(
                            question.copy(
                                questionText = questionText,
                                questionType = questionType,
                                optionA = if (questionType != QuestionType.TRUE_FALSE) optionA.ifBlank { null } else null,
                                optionB = if (questionType != QuestionType.TRUE_FALSE) optionB.ifBlank { null } else null,
                                optionC = if (questionType != QuestionType.TRUE_FALSE) optionC.ifBlank { null } else null,
                                optionD = if (questionType != QuestionType.TRUE_FALSE) optionD.ifBlank { null } else null,
                                correctAnswer = correctAnswer
                            )
                        )
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

