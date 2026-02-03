package com.example.english.ui.screens.deck_list

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.english.EnglishApp
import com.example.english.data.model.Deck
import com.example.english.data.repository.DeckRepository
import com.example.english.ui.ViewModelFactory
import com.example.english.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(navController: NavController) {
    val application = navController.context.applicationContext as EnglishApp

    // 使用 remember 缓存 Repository，避免重组时重复创建
    val deckRepository = remember {
        DeckRepository(
            application.database.deckDao(),
            application.database.wordDao(),
            application.database.questionDao()
        )
    }

    val viewModel: DeckListViewModel = viewModel(
        factory = remember { ViewModelFactory(application, deckRepository) }
    )
    val decks by viewModel.decks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }

    // 🔒 应用锁定状态监听（从 SettingsViewModel）
    val settingsViewModel: SettingsViewModel = viewModel()
    val isAppLocked by settingsViewModel.isAppLocked.collectAsState()
    val lockReason by settingsViewModel.lockReason.collectAsState()
    val restoreProgress by settingsViewModel.restoreProgress.collectAsState()

    // 🔒 拦截返回键（锁定时）
    androidx.activity.compose.BackHandler(enabled = isAppLocked) {
        // 锁定时不允许返回
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("英语学习") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 88.dp  // 增加底部padding避免与FAB重叠
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 复习入口卡片
            item {
                ReviewEntryCard(
                    onEnterClick = {
                        navController.navigate("review")
                    }
                )
            }

            items(decks) { deck ->
                DeckCard(
                    deck = deck,
                    onStudyClick = {
                        val destination = if (deck.deckType == com.example.english.data.model.DeckType.QUESTION) {
                            "question_setup/${deck.id}"
                        } else {
                            "study_setup/${deck.id}"
                        }
                        navController.navigate(destination)
                    },
                    onImportClick = {
                        val destination = if (deck.deckType == com.example.english.data.model.DeckType.QUESTION) {
                            "import_questions/${deck.id}"
                        } else {
                            "import_words/${deck.id}"
                        }
                        navController.navigate(destination)
                    },
                    onStudiedWordsClick = {
                        val destination = if (deck.deckType == com.example.english.data.model.DeckType.QUESTION) {
                            "practiced_questions/${deck.id}"
                        } else {
                            "studied_words/${deck.id}"
                        }
                        navController.navigate(destination)
                    },
                    onViewLibraryClick = {
                        val destination = if (deck.deckType == com.example.english.data.model.DeckType.QUESTION) {
                            "view_questions/${deck.id}"
                        } else {
                            "view_words/${deck.id}"
                        }
                        navController.navigate(destination)
                    },
                    onDeleteClick = {
                        deckToDelete = deck
                        showDeleteDialog = true
                    }
                )
            }
        }

        if (showDialog) {
            AddDeckDialog(
                onDismiss = { showDialog = false },
                onConfirm = { deckName, deckType ->
                    viewModel.addDeck(deckName, deckType)
                    showDialog = false
                }
            )
        }

        // 删除确认对话框
        if (showDeleteDialog && deckToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    deckToDelete = null
                },
                title = { Text("确认删除") },
                text = {
                    Text("确定要删除词库「${deckToDelete!!.name}」吗？\n\n此操作将删除词库及其中的所有单词/题目，且无法恢复。")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDeck(deckToDelete!!)
                            showDeleteDialog = false
                            deckToDelete = null
                        }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            deckToDelete = null
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
    }

        // 🔒 应用锁定遮罩（恢复备份期间）
        if (isAppLocked) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { }  // 拦截所有触摸事件
                    },
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp
                    )

                    Spacer(Modifier.height(32.dp))

                    Text(
                        text = lockReason,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    // 显示详细进度
                    if (restoreProgress.totalCount > 0) {
                        LinearProgressIndicator(
                            progress = { restoreProgress.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "${restoreProgress.percentage}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        Text(
                            text = restoreProgress.currentTable,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Text(
                            text = "${restoreProgress.currentCount}/${restoreProgress.totalCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddDeckDialog(onDismiss: () -> Unit, onConfirm: (String, com.example.english.data.model.DeckType) -> Unit) {
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(com.example.english.data.model.DeckType.VOCABULARY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建新词库") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("词库名称") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "选择模式",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == com.example.english.data.model.DeckType.VOCABULARY,
                        onClick = { selectedType = com.example.english.data.model.DeckType.VOCABULARY },
                        label = { Text("背单词") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == com.example.english.data.model.DeckType.QUESTION,
                        onClick = { selectedType = com.example.english.data.model.DeckType.QUESTION },
                        label = { Text("刷题") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text, selectedType)
                    }
                }
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun DeckCard(
    deck: Deck,
    onStudyClick: () -> Unit,
    onImportClick: () -> Unit,
    onStudiedWordsClick: () -> Unit,
    onViewLibraryClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isQuestionMode = deck.deckType == com.example.english.data.model.DeckType.QUESTION
    val itemLabel = if (isQuestionMode) "题目" else "单词"

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = deck.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // 模式标签
                Surface(
                    color = if (isQuestionMode)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (isQuestionMode) "刷题" else "背单词",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (deck.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deck.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${itemLabel}数: ${deck.wordCount}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 第一行按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStudyClick,
                    modifier = Modifier.weight(1f),
                    enabled = deck.wordCount > 0
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isQuestionMode) "刷题" else "学习")
                }

                OutlinedButton(
                    onClick = onImportClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导入")
                }
            }

            // 第二行按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStudiedWordsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isQuestionMode) "已刷题目" else "已学单词")
                }

                OutlinedButton(
                    onClick = onViewLibraryClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isQuestionMode) "查看题库" else "查看词库")
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 复习入口卡片
 * 注意：只有"进入"按钮可以点击，卡片本身不响应点击
 */
@Composable
private fun ReviewEntryCard(
    onEnterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        // Card 默认不可点击，除非添加 onClick 参数
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "📚 复习中心",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "查看学习记录和复习任务",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onEnterClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("进入")
            }
        }
    }
}
