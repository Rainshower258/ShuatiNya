package com.example.english.ui.screens.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.english.EnglishApp
import com.example.english.data.repository.DeckRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySetupScreen(
    navController: NavController,
    deckId: Long
) {
    val application = LocalContext.current.applicationContext as EnglishApp

    // 使用 remember 缓存 Repository，避免重组时重复创建
    val deckRepository = remember {
        val database = application.database
        DeckRepository(database.deckDao(), database.wordDao())
    }

    val viewModel: StudySetupViewModel = viewModel(
        factory = remember { StudySetupViewModelFactory(deckRepository) }
    )

    val deck by viewModel.deck.collectAsState()
    val wordCount by viewModel.wordCount.collectAsState()
    var selectedCount by remember { mutableIntStateOf(10) }
    var customCount by remember { mutableStateOf("") }
    var useCustomCount by remember { mutableStateOf(false) }

    val countOptions = listOf(5, 10, 15, 20, 25, 30)

    LaunchedEffect(deckId) {
        viewModel.loadDeck(deckId)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开始学习") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 词库信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = deck?.name ?: "加载中...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "总单词数: $wordCount",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!deck?.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = deck!!.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 学习数量选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "今日学习数量",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        countOptions.forEach { count ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (!useCustomCount && selectedCount == count),
                                        onClick = {
                                            selectedCount = count
                                            useCustomCount = false
                                        },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (!useCustomCount && selectedCount == count),
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "$count 个单词",
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // 自定义数量选项
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = useCustomCount,
                                    onClick = { useCustomCount = true },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = useCustomCount,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "自定义：",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = customCount,
                                onValueChange = {
                                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                        customCount = it
                                        useCustomCount = true
                                    }
                                },
                                modifier = Modifier
                                    .width(100.dp),
                                placeholder = { Text("输入数量") },
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // 学习模式说明
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "学习说明",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 优先复习到期的单词\n" +
                               "• 错误的单词会在最后重新练习\n" +
                               "• 系统会根据遗忘曲线智能安排复习",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 开始学习按钮
            Button(
                onClick = {
                    val finalCount = if (useCustomCount) {
                        customCount.toIntOrNull() ?: 10
                    } else {
                        selectedCount
                    }
                    navController.navigate("study/$deckId/$finalCount")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = wordCount > 0 && (!useCustomCount || customCount.isNotEmpty())
            ) {
                Text(
                    text = if (wordCount > 0) "开始学习" else "该词库暂无单词",
                    fontSize = 18.sp
                )
            }
        }
    }
}
