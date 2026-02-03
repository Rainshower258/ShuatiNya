package com.example.english.ui.screens.study

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.example.english.EnglishApp
import com.example.english.data.model.ChoiceOption
import com.example.english.data.model.WordStudyState
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.WordRepository
import com.example.english.data.service.StudyService
import com.example.english.util.CompletionGifProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    navController: NavController,
    deckId: Long,
    plannedCount: Int
) {
    val application = LocalContext.current.applicationContext as EnglishApp

    // 使用 remember 缓存 Service 和 Repository，避免重组时重复创建
    val studyService = remember {
        val database = application.database
        StudyService(database.wordDao(), database.studySessionDao())
    }
    val deckRepository = remember {
        val database = application.database
        DeckRepository(database.deckDao(), database.wordDao())
    }
    val wordRepository = remember {
        val database = application.database
        WordRepository(database.wordDao())
    }

    val viewModel: StudyViewModel = viewModel(
        factory = remember { StudyViewModelFactory(studyService, deckRepository, wordRepository) }
    )

    val studyWords by viewModel.studyWords.collectAsState()
    val currentWordIndex by viewModel.currentWordIndex.collectAsState()
    val choices by viewModel.choices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentPhase by viewModel.currentPhase.collectAsState()
    val currentWord = if (currentWordIndex < studyWords.size) studyWords[currentWordIndex] else null

    LaunchedEffect(deckId, plannedCount) {
        viewModel.startStudySession(deckId, plannedCount)
    }

    // 根据学习阶段确定标题
    val titleText = when (currentPhase) {
        com.example.english.data.service.StudyPhase.MAIN_STUDY -> "单词学习"
        com.example.english.data.service.StudyPhase.PHRASE_REVIEW -> "短语复习"
        com.example.english.data.service.StudyPhase.WRONG_WORD_REVIEW -> "错题复习"
        com.example.english.data.service.StudyPhase.COMPLETED -> "学习完成"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(titleText, fontSize = 18.sp)
                        if (studyWords.isNotEmpty()) {
                            Text(
                                "进度: ${currentWordIndex + 1}/${studyWords.size}",
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
                },
                actions = {
                    IconButton(onClick = { /* TODO: 打开设置 */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
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
                currentWord != null -> {
                    // 根据题型显示不同的UI
                    if (currentWord.questionType == com.example.english.data.model.WordQuestionType.PHRASE_RECOGNITION) {
                        // 短语识别模式
                        PhraseRecognitionContent(
                            word = currentWord,
                            onAnswer = { userKnows ->
                                viewModel.handlePhraseAnswer(userKnows)
                            },
                            onNextWord = {
                                viewModel.moveToNextWord()
                            },
                            onPreviousWord = {
                                viewModel.moveToPreviousWord()
                            },
                            canGoBack = currentWordIndex > 0,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // 单词选择题模式
                        StudyContent(
                            word = currentWord,
                            choices = choices,
                            onChoiceSelected = { choice ->
                                viewModel.handleChoice(choice)
                            },
                            onNextWord = {
                                viewModel.moveToNextWord()
                            },
                            onPreviousWord = {
                                viewModel.moveToPreviousWord()
                            },
                            canGoBack = currentWordIndex > 0,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                else -> {
                    // 学习完成
                    StudyCompleteContent(
                        onFinish = {
                            viewModel.finishStudy()
                            navController.navigateUp()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyContent(
    word: WordStudyState,
    choices: List<ChoiceOption>,
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
                    text = word.word.english,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (word.isAnswered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = word.word.phonetic,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = word.word.partOfSpeech,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = word.word.chinese,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (word.isCorrect)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 选择题区域
        if (!word.isAnswered && choices.isNotEmpty()) {
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

            if (word.isAnswered) {
                Button(onClick = onNextWord) {
                    Text("下一词")
                }
            }
        }
    }
}

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

@Composable
private fun StudyCompleteContent(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val randomGifFileName = remember { CompletionGifProvider.getRandomGif() }

    // 创建支持GIF的ImageLoader
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    // 创建图片请求
    val imageRequest = remember {
        ImageRequest.Builder(context)
            .data("file:///android_asset/$randomGifFileName")
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        imageLoader = imageLoader
    )

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 显示随机GIF动画
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Image(
                painter = painter,
                contentDescription = "完成动画",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "🎉",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "学习完成！",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "恭喜你完成了今天的学习任务",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("完成", fontSize = 18.sp)
        }
    }
}

/**
 * 短语识别界面（认识/不认识模式）
 */
@Composable
private fun PhraseRecognitionContent(
    word: WordStudyState,
    onAnswer: (Boolean) -> Unit,
    onNextWord: () -> Unit,
    onPreviousWord: () -> Unit,
    canGoBack: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 短语显示区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 短语标识
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "短语",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 短语文本
                Text(
                    text = word.word.english,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // 如果已回答，显示释义和用法
                if (word.isAnswered) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "释义",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = word.word.chinese,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (word.isCorrect)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )

                            // 用法示例
                            word.word.phraseUsage?.let { usage ->
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "用法示例",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = usage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 认识/不认识按钮
        if (!word.isAnswered) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onAnswer(false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("不认识", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onAnswer(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("认识", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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

            if (word.isAnswered) {
                Button(onClick = onNextWord) {
                    Text("下一词")
                }
            }
        }
    }
}
