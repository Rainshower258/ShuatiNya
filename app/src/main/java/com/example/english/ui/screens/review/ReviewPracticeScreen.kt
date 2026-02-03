package com.example.english.ui.screens.review

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.english.EnglishApp
import com.example.english.data.model.ChoiceOption
import com.example.english.data.model.ReviewWordState

/**
 * 复习练习界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewPracticeScreen(
    navController: NavController
) {
    val application = LocalContext.current.applicationContext as EnglishApp
    val database = remember { application.database }

    val viewModel: ReviewPracticeViewModel = viewModel(
        factory = remember { ReviewPracticeViewModelFactory(database.wordDao()) }
    )

    val reviewWords by viewModel.reviewWords.collectAsState()
    val currentWordIndex by viewModel.currentWordIndex.collectAsState()
    val choices by viewModel.choices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showAnswer by viewModel.showAnswer.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startReview()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("复习练习", fontSize = 18.sp)
                        if (reviewWords.isNotEmpty()) {
                            val (current, total) = viewModel.getProgress()
                            Text(
                                "进度: $current/$total",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                reviewWords.isEmpty() -> {
                    EmptyReviewContent(
                        onFinish = { navController.navigateUp() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                currentWordIndex < reviewWords.size -> {
                    val currentWord = reviewWords[currentWordIndex]
                    ReviewContent(
                        wordState = currentWord,
                        choices = choices,
                        showAnswer = showAnswer,
                        onChoiceSelected = { choice ->
                            viewModel.handleChoice(choice)
                        },
                        onNextWord = {
                            if (viewModel.isCompleted()) {
                                navController.navigateUp()
                            } else {
                                viewModel.moveToNextWord()
                            }
                        },
                        onPreviousWord = {
                            viewModel.moveToPreviousWord()
                        },
                        canGoBack = currentWordIndex > 0,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    ReviewCompleteContent(
                        onFinish = { navController.navigateUp() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * 复习内容
 */
@Composable
private fun ReviewContent(
    wordState: ReviewWordState,
    choices: List<ChoiceOption>,
    showAnswer: Boolean,
    onChoiceSelected: (ChoiceOption) -> Unit,
    onNextWord: () -> Unit,
    onPreviousWord: () -> Unit,
    canGoBack: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 单词显示区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = wordState.word.english,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (showAnswer) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = wordState.word.phonetic,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = wordState.word.partOfSpeech,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = wordState.word.chinese,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (wordState.isRemembered == true)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "复习阶段: ${wordState.reviewStage}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 选择题区域
        if (!showAnswer && choices.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(choices) { choice ->
                    ChoiceButton(
                        choice = choice,
                        onClick = { onChoiceSelected(choice) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onPreviousWord,
                enabled = canGoBack
            ) {
                Text("上一词")
            }

            if (showAnswer) {
                Button(onClick = onNextWord) {
                    Text("下一词")
                }
            }
        }
    }
}

/**
 * 选择按钮
 */
@Composable
private fun ChoiceButton(
    choice: ChoiceOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = choice.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = choice.partOfSpeech,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 空复习内容
 */
@Composable
private fun EmptyReviewContent(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✨",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无需要复习的单词",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "继续学习新单词吧！",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onFinish) {
            Text("返回")
        }
    }
}

/**
 * 复习完成内容
 */
@Composable
private fun ReviewCompleteContent(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎉",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "复习完成！",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "继续保持！",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onFinish) {
            Text("完成")
        }
    }
}

