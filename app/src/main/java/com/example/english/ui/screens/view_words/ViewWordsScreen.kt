package com.example.english.ui.screens.view_words

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.english.EnglishApp
import com.example.english.data.model.Word
import com.example.english.data.model.WordType
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.WordRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewWordsScreen(
    deckId: Long,
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as EnglishApp

    // 使用 remember 缓存 Repository，避免重组时重复创建
    val (wordRepository, deckRepository) = remember {
        val database = application.database
        Pair(
            WordRepository(database.wordDao()),
            DeckRepository(database.deckDao(), database.wordDao())
        )
    }

    val viewModel: ViewWordsViewModel = viewModel(
        factory = remember { ViewWordsViewModelFactory(wordRepository, deckRepository) }
    )
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingWord by remember { mutableStateOf<Word?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 字母分组
    val groupedWords = remember(uiState.filteredWords) {
        uiState.filteredWords.groupBy {
            it.english.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
        }.toSortedMap()
    }

    val alphabet = remember(groupedWords) {
        groupedWords.keys.toList()
    }

    LaunchedEffect(deckId) {
        viewModel.loadWords(deckId)
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
                    if (uiState.selectedWords.isNotEmpty()) {
                        Text(
                            text = "已选 ${uiState.selectedWords.size}",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconButton(onClick = { viewModel.selectAllWords() }) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 搜索框
                OutlinedTextField(
                    value = uiState.searchKeyword,
                    onValueChange = { viewModel.onSearchKeywordChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("搜索单词...") },
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

                // 单词列表
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.filteredWords.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.searchKeyword.isBlank()) "暂无单词" else "未找到匹配的单词",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 48.dp, top = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupedWords.forEach { (letter, words) ->
                            // 字母分组标题
                            item(key = "header_$letter") {
                                Text(
                                    text = letter,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            // 该字母下的单词
                            items(words, key = { it.id }) { word ->
                                WordItem(
                                    word = word,
                                    isSelected = word.id in uiState.selectedWords,
                                    onToggleSelection = { viewModel.toggleWordSelection(word.id) },
                                    onEdit = {
                                        editingWord = word
                                        showEditDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 侧边字母索引条
            if (alphabet.isNotEmpty() && !uiState.isLoading) {
                AlphabetIndexBar(
                    alphabet = alphabet,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    onLetterClick = { letter ->
                        coroutineScope.launch {
                            val index = groupedWords.keys.indexOf(letter)
                            if (index >= 0) {
                                // 计算滚动位置（包括之前的分组）
                                var itemIndex = 0
                                groupedWords.keys.take(index).forEach { key ->
                                    itemIndex += 1 // header
                                    itemIndex += groupedWords[key]?.size ?: 0 // items
                                }
                                listState.animateScrollToItem(itemIndex)
                            }
                        }
                    }
                )
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除选中的 ${uiState.selectedWords.size} 个单词吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedWords()
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

    // 编辑对话框
    if (showEditDialog && editingWord != null) {
        EditWordDialog(
            word = editingWord!!,
            onDismiss = {
                showEditDialog = false
                editingWord = null
            },
            onConfirm = { updatedWord ->
                viewModel.updateWord(updatedWord)
                showEditDialog = false
                editingWord = null
            }
        )
    }
}

@Composable
fun WordItem(
    word: Word,
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
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 英文单词
                    Text(
                        text = word.english,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // 类型标识
                    if (word.wordType == WordType.PHRASE) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "短语",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
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

                // 音标和词性
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (word.phonetic.isNotBlank()) {
                        Text(
                            text = word.phonetic,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = word.partOfSpeech,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 中文释义
                Text(
                    text = word.chinese,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 短语用法（如果有）
                if (word.phraseUsage != null && word.phraseUsage.isNotBlank()) {
                    Text(
                        text = "用法: ${word.phraseUsage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // 学习统计
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "正确: ${word.correctCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "错误: ${word.wrongCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336)
                    )
                    if (word.reviewStage > 0) {
                        Text(
                            text = "复习阶段: ${word.reviewStage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 选择指示器 - 始终显示
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterVertically)
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
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        ),
                        color = Color.Transparent
                    ) {}
                }
            }
        }
    }
}

// 侧边字母索引条（支持滑动）
@Composable
fun AlphabetIndexBar(
    alphabet: List<String>,
    modifier: Modifier = Modifier,
    onLetterClick: (String) -> Unit
) {
    var selectedLetter by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .width(24.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 4.dp)
            .pointerInput(alphabet) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val index = (offset.y / size.height * alphabet.size).toInt()
                            .coerceIn(0, alphabet.lastIndex)
                        val letter = alphabet[index]
                        selectedLetter = letter
                        onLetterClick(letter)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val index = (change.position.y / size.height * alphabet.size).toInt()
                            .coerceIn(0, alphabet.lastIndex)
                        val letter = alphabet[index]
                        if (letter != selectedLetter) {
                            selectedLetter = letter
                            onLetterClick(letter)
                        }
                    },
                    onDragEnd = {
                        selectedLetter = null
                    },
                    onDragCancel = {
                        selectedLetter = null
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        alphabet.forEach { letter ->
            Text(
                text = letter,
                fontSize = 10.sp,
                color = if (letter == selectedLetter)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onLetterClick(letter) }
                    .background(
                        if (letter == selectedLetter)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(vertical = 2.dp, horizontal = 4.dp)
            )
        }
    }
}

// 编辑单词对话框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWordDialog(
    word: Word,
    onDismiss: () -> Unit,
    onConfirm: (Word) -> Unit
) {
    var english by remember { mutableStateOf(word.english) }
    var chinese by remember { mutableStateOf(word.chinese) }
    var partOfSpeech by remember { mutableStateOf(word.partOfSpeech) }
    var phonetic by remember { mutableStateOf(word.phonetic) }
    var wordType by remember { mutableStateOf(word.wordType) }
    var phraseUsage by remember { mutableStateOf(word.phraseUsage ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑单词") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 类型选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = wordType == WordType.WORD,
                        onClick = { wordType = WordType.WORD },
                        label = { Text("单词") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = wordType == WordType.PHRASE,
                        onClick = { wordType = WordType.PHRASE },
                        label = { Text("短语") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = english,
                    onValueChange = { english = it },
                    label = { Text("英文") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = chinese,
                    onValueChange = { chinese = it },
                    label = { Text("中文释义") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = partOfSpeech,
                    onValueChange = { partOfSpeech = it },
                    label = { Text("词性") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (wordType == WordType.WORD) {
                    OutlinedTextField(
                        value = phonetic,
                        onValueChange = { phonetic = it },
                        label = { Text("音标") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (wordType == WordType.PHRASE) {
                    OutlinedTextField(
                        value = phraseUsage,
                        onValueChange = { phraseUsage = it },
                        label = { Text("用法示例") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (english.isNotBlank() && chinese.isNotBlank()) {
                        onConfirm(
                            word.copy(
                                english = english,
                                chinese = chinese,
                                partOfSpeech = partOfSpeech,
                                phonetic = if (wordType == WordType.WORD) phonetic else "",
                                wordType = wordType,
                                phraseUsage = if (wordType == WordType.PHRASE) phraseUsage.ifBlank { null } else null
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

