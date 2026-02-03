package com.example.english.ui.screens.question_practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.english.data.model.QuestionType
import com.example.english.data.model.QuestionStudyState
import com.example.english.data.service.QuestionPracticeService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionPracticeScreen(
    navController: NavController,
    deckId: Long,
    plannedCount: Int
) {
    val application = LocalContext.current.applicationContext as EnglishApp

    // 使用 remember 缓存 Service，避免重组时重复创建
    val practiceService = remember {
        val database = application.database
        QuestionPracticeService(database.questionDao())
    }

    val viewModel: QuestionPracticeViewModel = viewModel(
        factory = remember { QuestionPracticeViewModelFactory(practiceService) }
    )

    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()

    val currentQuestion = if (currentIndex < questions.size) questions[currentIndex] else null

    LaunchedEffect(deckId, plannedCount) {
        viewModel.startPractice(deckId, plannedCount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!isCompleted) {
                        Text("刷题 ${currentIndex + 1}/${questions.size}")
                    } else {
                        Text("练习完成")
                    }
                },
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
        } else if (isCompleted) {
            CompletionScreen(
                questions = questions,
                onBackClick = { navController.navigateUp() },
                modifier = Modifier.padding(paddingValues)
            )
        } else if (currentQuestion != null) {
            QuestionContent(
                question = currentQuestion,
                currentIndex = currentIndex,
                onAnswerSubmit = { answer ->
                    viewModel.submitAnswer(currentQuestion.question.id, answer)
                },
                onNext = {
                    viewModel.moveToNext()
                },
                onPrevious = {
                    viewModel.moveToPrevious()
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun QuestionContent(
    question: QuestionStudyState,
    currentIndex: Int,
    onAnswerSubmit: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAnswer by remember(question.question.id) { mutableStateOf("") }
    var selectedMultiple by remember(question.question.id) { mutableStateOf(setOf<String>()) }
    val showResult = question.isCompleted

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 题型标签
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = when (question.question.questionType) {
                    QuestionType.SINGLE_CHOICE -> "单选题"
                    QuestionType.MULTIPLE_CHOICE -> "多选题"
                    QuestionType.TRUE_FALSE -> "判断题"
                },
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // 题目内容（自适应字体大小，支持滚动）
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 300.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = question.question.questionText,
                    fontSize = if (question.question.questionText.length > 100) 14.sp else 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(20.dp),
                    lineHeight = 24.sp
                )
            }
        }

        // 选项区域
        when (question.question.questionType) {
            QuestionType.SINGLE_CHOICE -> {
                SingleChoiceOptions(
                    question = question,
                    selectedAnswer = selectedAnswer,
                    onAnswerSelect = { selectedAnswer = it },
                    showResult = showResult
                )
            }
            QuestionType.MULTIPLE_CHOICE -> {
                MultipleChoiceOptions(
                    question = question,
                    selectedAnswers = selectedMultiple,
                    onAnswerSelect = { option ->
                        selectedMultiple = if (selectedMultiple.contains(option)) {
                            selectedMultiple - option
                        } else {
                            selectedMultiple + option
                        }
                    },
                    showResult = showResult
                )
            }
            QuestionType.TRUE_FALSE -> {
                TrueFalseOptions(
                    question = question,
                    selectedAnswer = selectedAnswer,
                    onAnswerSelect = { selectedAnswer = it },
                    showResult = showResult
                )
            }
        }


        Spacer(modifier = Modifier.weight(1f))

        // 按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 上一题按钮
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
                enabled = currentIndex > 0
            ) {
                Text("上一题", fontSize = 16.sp)
            }

            // 提交答案/下一题按钮
            if (!showResult) {
                Button(
                    onClick = {
                        val answer = when (question.question.questionType) {
                            QuestionType.SINGLE_CHOICE -> selectedAnswer
                            QuestionType.MULTIPLE_CHOICE -> selectedMultiple.sorted().joinToString("")
                            QuestionType.TRUE_FALSE -> selectedAnswer
                        }
                        onAnswerSubmit(answer)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = when (question.question.questionType) {
                        QuestionType.SINGLE_CHOICE -> selectedAnswer.isNotEmpty()
                        QuestionType.MULTIPLE_CHOICE -> selectedMultiple.isNotEmpty()
                        QuestionType.TRUE_FALSE -> selectedAnswer.isNotEmpty()
                    }
                ) {
                    Text("提交答案", fontSize = 16.sp)
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("下一题", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun SingleChoiceOptions(
    question: QuestionStudyState,
    selectedAnswer: String,
    onAnswerSelect: (String) -> Unit,
    showResult: Boolean
) {
    val options = listOf(
        "A" to question.question.optionA,
        "B" to question.question.optionB,
        "C" to question.question.optionC,
        "D" to question.question.optionD
    ).filter { it.second != null }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (id, text) ->
            OptionCard(
                id = id,
                text = text!!,
                isSelected = selectedAnswer == id,
                isCorrect = showResult && question.question.correctAnswer == id,
                isWrong = showResult && selectedAnswer == id && question.question.correctAnswer != id,
                enabled = !showResult,
                onClick = { onAnswerSelect(id) }
            )
        }
    }
}

@Composable
private fun MultipleChoiceOptions(
    question: QuestionStudyState,
    selectedAnswers: Set<String>,
    onAnswerSelect: (String) -> Unit,
    showResult: Boolean
) {
    val options = listOf(
        "A" to question.question.optionA,
        "B" to question.question.optionB,
        "C" to question.question.optionC,
        "D" to question.question.optionD
    ).filter { it.second != null }

    val correctSet = question.question.correctAnswer.toSet().map { it.toString() }.toSet()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (id, text) ->
            OptionCard(
                id = id,
                text = text!!,
                isSelected = selectedAnswers.contains(id),
                isCorrect = showResult && correctSet.contains(id),
                isWrong = showResult && selectedAnswers.contains(id) && !correctSet.contains(id),
                enabled = !showResult,
                onClick = { onAnswerSelect(id) }
            )
        }
        if (!showResult) {
            Text(
                text = "多选题：可选择多个选项",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrueFalseOptions(
    question: QuestionStudyState,
    selectedAnswer: String,
    onAnswerSelect: (String) -> Unit,
    showResult: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OptionCard(
            id = "true",
            text = "正确",
            isSelected = selectedAnswer == "true",
            isCorrect = showResult && question.question.correctAnswer == "true",
            isWrong = showResult && selectedAnswer == "true" && question.question.correctAnswer != "true",
            enabled = !showResult,
            onClick = { onAnswerSelect("true") }
        )
        OptionCard(
            id = "false",
            text = "错误",
            isSelected = selectedAnswer == "false",
            isCorrect = showResult && question.question.correctAnswer == "false",
            isWrong = showResult && selectedAnswer == "false" && question.question.correctAnswer != "false",
            enabled = !showResult,
            onClick = { onAnswerSelect("false") }
        )
    }
}

@Composable
private fun OptionCard(
    id: String,
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect -> MaterialTheme.colorScheme.primaryContainer
        isWrong -> MaterialTheme.colorScheme.errorContainer
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp, max = 200.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        enabled = enabled,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$id. ",
                fontWeight = FontWeight.Bold,
                fontSize = if (text.length > 50) 14.sp else 16.sp
            )
            Text(
                text = text,
                fontSize = if (text.length > 50) 14.sp else 16.sp,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
            if (isCorrect) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "正确",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else if (isWrong) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "错误",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CompletionScreen(
    questions: List<QuestionStudyState>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val correctCount = questions.count { it.isCorrect == true }
    val totalCount = questions.size
    val accuracy = if (totalCount > 0) (correctCount.toFloat() / totalCount * 100).toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✅",
            fontSize = 72.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "练习完成",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "本次成绩",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$correctCount / $totalCount",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "正确率: $accuracy%",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("返回", fontSize = 16.sp)
        }
    }
}

