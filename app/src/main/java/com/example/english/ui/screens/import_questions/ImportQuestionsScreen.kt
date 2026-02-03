package com.example.english.ui.screens.import_questions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.english.EnglishApp
import com.example.english.data.parser.QuestionParser
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.QuestionRepository
import com.example.english.util.ClipboardHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportQuestionsScreen(
    navController: NavController,
    deckId: Long
) {
    val context = LocalContext.current
    val application = context.applicationContext as EnglishApp

    // 使用 remember 缓存 Repository，避免重组时重复创建
    val (deckRepository, questionRepository) = remember {
        val database = application.database
        Pair(
            DeckRepository(database.deckDao(), database.wordDao()),
            QuestionRepository(database.questionDao())
        )
    }

    val viewModel: ImportQuestionsViewModel = viewModel(
        factory = remember { ImportQuestionsViewModelFactory(deckRepository, questionRepository) }
    )

    val deck by viewModel.deck.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showFormatHelp by remember { mutableStateOf(false) }

    // Snackbar 相关
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(deckId) {
        viewModel.loadDeck(deckId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入题目") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showFormatHelp = true }) {
                        Icon(Icons.Default.Info, contentDescription = "格式说明")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 题库信息
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "目标题库",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = deck?.name ?: "加载中...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // AI 提示词卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI 提示词",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Button(
                            onClick = {
                                val prompt = QuestionParser.getAIPrompt()
                                ClipboardHelper.copyToClipboard(context, prompt)
                                scope.launch {
                                    snackbarHostState.showSnackbar("已复制到剪贴板")
                                }
                            },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("复制")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击复制按钮，将提示词发送给 AI（如 ChatGPT），然后将生成的题目粘贴到下方输入框。",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // 题目输入区域
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                label = { Text("粘贴题目文本") },
                placeholder = { Text("在此粘贴从 AI 复制的题目...") },
                maxLines = 15
            )

            // 从剪贴板粘贴按钮
            OutlinedButton(
                onClick = {
                    val clipboardText = ClipboardHelper.readClipboard(context)
                    if (clipboardText != null) {
                        inputText = clipboardText
                        scope.launch {
                            snackbarHostState.showSnackbar("已从剪贴板粘贴")
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("剪贴板为空")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("从剪贴板粘贴")
            }

            // 导入结果显示
            importResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.failureCount == 0)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (result.failureCount == 0) Icons.Default.Check else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (result.failureCount == 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "导入完成",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("成功: ${result.successCount} 题")
                        if (result.duplicateCount > 0) {
                            Text("跳过重复: ${result.duplicateCount} 题")
                        }
                        if (result.failureCount > 0) {
                            Text("失败: ${result.failureCount} 题")
                        }
                        if (result.errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "错误信息:",
                                fontWeight = FontWeight.Medium
                            )
                            result.errors.take(3).forEach { error ->
                                Text("• $error", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 导入按钮
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.importQuestions(inputText, deckId)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImporting && inputText.isNotBlank()
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isImporting) "导入中..." else "开始导入")
            }
        }

        // 格式说明对话框
        if (showFormatHelp) {
            AlertDialog(
                onDismissRequest = { showFormatHelp = false },
                title = { Text("题目格式说明") },
                text = {
                    SelectionContainer {
                        Text(
                            text = QuestionParser.SUPPORTED_FORMATS,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFormatHelp = false }) {
                        Text("关闭")
                    }
                }
            )
        }
    }
}

