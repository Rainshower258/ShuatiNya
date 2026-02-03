package com.example.english.ui.screens.import_words

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
import com.example.english.data.parser.WordParser
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.WordRepository
import com.example.english.util.ClipboardHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportWordsScreen(
    navController: NavController,
    deckId: Long
) {
    val context = LocalContext.current
    val application = context.applicationContext as EnglishApp

    // 使用 remember 缓存 Repository，避免重组时重复创建
    val (deckRepository, wordRepository) = remember {
        val database = application.database
        Pair(
            DeckRepository(database.deckDao(), database.wordDao()),
            WordRepository(database.wordDao())
        )
    }

    val viewModel: ImportWordsViewModel = viewModel(
        factory = remember { ImportWordsViewModelFactory(deckRepository, wordRepository) }
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
                title = { Text("导入单词") },
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
            // 词库信息
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "目标词库",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = deck?.name ?: "加载中...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // AI Prompt 复制区域
            Card(
                modifier = Modifier.fillMaxWidth()
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

                        // 复制按钮
                        FilledTonalButton(
                            onClick = {
                                val success = ClipboardHelper.copyToClipboard(
                                    context = context,
                                    text = WordParser.getAIPrompt(),
                                    label = "词条提取 Prompt"
                                )

                                scope.launch {
                                    if (success) {
                                        snackbarHostState.showSnackbar(
                                            message = "✅ 已复制到剪贴板",
                                            duration = SnackbarDuration.Short
                                        )
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            message = "❌ 复制失败，请重试",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "复制",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("复制", fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "复制以下提示词发送给 AI（ChatGPT/Claude/Gemini 等），然后将 AI 生成的结果粘贴到下方输入框",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Prompt 预览（可选中）
                    SelectionContainer {
                        Text(
                            text = WordParser.getAIPrompt(),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState()),
                            maxLines = 8
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 提示信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "提示：复制后请直接粘贴，避免使用输入法的剪贴板历史",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // 输入区域
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "粘贴单词文本",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = {
                            Text("请粘贴符合格式的单词文本...\n\n例如：\n英文：hello\n中文对照：你好\n词性：interj.\n音标：/həˈloʊ/")
                        },
                        maxLines = Int.MAX_VALUE
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "支持的格式：文本粘贴、TXT文件、CSV文件、TSV文件",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 文件导入按钮
            OutlinedButton(
                onClick = {
                    // TODO: 实现文件选择
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("选择文件导入")
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
                        Text(
                            text = "导入结果",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("成功导入: ${result.successCount} 个单词")

                        if (result.duplicateCount > 0) {
                            Text(
                                text = "跳过重复: ${result.duplicateCount} 个",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "（相同英文和词性的单词已存在）",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (result.failureCount > 0) {
                            Text("导入失败: ${result.failureCount} 个")
                            if (result.errors.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "错误详情:",
                                    fontWeight = FontWeight.Medium
                                )
                                result.errors.take(5).forEach { error ->
                                    Text(
                                        text = "• $error",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (result.errors.size > 5) {
                                    Text(
                                        text = "... 还有 ${result.errors.size - 5} 个错误",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 导入按钮
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.importWordsFromText(inputText, deckId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = inputText.isNotBlank() && !isImporting
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导入中...")
                } else {
                    Text("开始导入", fontSize = 16.sp)
                }
            }
        }
    }

    // 格式说明对话框
    if (showFormatHelp) {
        AlertDialog(
            onDismissRequest = { showFormatHelp = false },
            title = { Text("导入格式说明") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        Text(
                            text = WordParser.SUPPORTED_FORMATS,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
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
