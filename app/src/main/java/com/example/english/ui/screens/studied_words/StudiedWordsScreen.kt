package com.example.english.ui.screens.studied_words

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.english.EnglishApp
import com.example.english.data.model.Word
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.WordRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudiedWordsScreen(
    navController: NavController,
    deckId: Long
) {
    val application = LocalContext.current.applicationContext as EnglishApp

    // 使用 remember 缓存 Repository，避免重组时重复创建
    val (deckRepository, wordRepository) = remember {
        val database = application.database
        Pair(
            DeckRepository(database.deckDao(), database.wordDao()),
            WordRepository(database.wordDao())
        )
    }

    val viewModel: StudiedWordsViewModel = viewModel(
        factory = remember { StudiedWordsViewModelFactory(deckRepository, wordRepository) }
    )

    val deck by viewModel.deck.collectAsState()
    val studiedWords by viewModel.studiedWords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(deckId) {
        viewModel.loadStudiedWords(deckId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("已学单词", fontSize = 18.sp)
                        deck?.let {
                            Text(
                                text = it.name,
                                fontSize = 14.sp,
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
                studiedWords.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📚",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "还没有学习过的单词",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "开始学习后，这里会显示你学过的单词",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "共 ${studiedWords.size} 个单词",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(studiedWords) { word ->
                            WordItem(word = word)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordItem(
    word: Word,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 英文单词
            Text(
                text = word.english,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // 中文翻译
            Text(
                text = word.chinese,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
