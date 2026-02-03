package com.example.english.ui.screens.question_practice

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
import com.example.english.data.repository.QuestionRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionSetupScreen(
    navController: NavController,
    deckId: Long
) {
    val application = LocalContext.current.applicationContext as EnglishApp

    // 使用 remember 缓存 Repository，避免重组时重复创建
    val (deckRepository, questionRepository) = remember {
        val database = application.database
        Pair(
            DeckRepository(database.deckDao(), database.wordDao()),
            QuestionRepository(database.questionDao())
        )
    }

    val viewModel: QuestionSetupViewModel = viewModel(
        factory = remember { QuestionSetupViewModelFactory(deckRepository, questionRepository) }
    )

    val deck by viewModel.deck.collectAsState()
    val questionCount by viewModel.questionCount.collectAsState()
    var selectedCount by remember { mutableIntStateOf(10) }
    var customCount by remember { mutableStateOf("") }
    var useCustomCount by remember { mutableStateOf(false) }

    val countOptions = listOf(5, 10, 15, 20, 25, 30)

    val scrollState = rememberScrollState()

    LaunchedEffect(deckId) {
        viewModel.loadDeck(deckId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开始刷题") },
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
            // 题库信息
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
                        text = "总题目数: $questionCount",
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

            // 刷题数量选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "今日刷题数量",
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
                                    text = "$count 道题",
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

            // 刷题模式说明
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
                        text = "刷题说明",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 支持单选题、多选题和判断题\n" +
                               "• 优先复习错题和需要巩固的题目\n" +
                               "• 系统自动记录答题统计",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            // 开始刷题按钮
            Button(
                onClick = {
                    val finalCount = if (useCustomCount) {
                        customCount.toIntOrNull() ?: 10
                    } else {
                        selectedCount
                    }
                    navController.navigate("question_practice/$deckId/$finalCount")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = questionCount > 0 && (!useCustomCount || customCount.isNotEmpty())
            ) {
                Text(
                    text = if (questionCount > 0) "开始刷题" else "该题库暂无题目",
                    fontSize = 18.sp
                )
            }
        }
    }
}

