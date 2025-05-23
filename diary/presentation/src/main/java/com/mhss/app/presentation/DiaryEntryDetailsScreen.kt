package com.mhss.app.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Mood
import com.mhss.app.presentation.components.AiQuestionSheet
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.DateTimeDialog
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.toUserMessage
import com.mhss.app.util.date.fullDate
import com.mhss.app.util.date.now
import com.mikepenz.markdown.coil2.Coil2ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.mhss.app.presentation.components.GradientIconButton
import com.mhss.app.presentation.components.SttBottomSheet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryDetailsScreen(
    navController: NavHostController,
    entryId: Int,
    viewModel: DiaryDetailsViewModel = koinViewModel(parameters = { parametersOf(entryId) })
) {
    val state = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    var openDialog by rememberSaveable { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf(Mood.OKAY) }
    var date by remember { mutableLongStateOf(now()) }
    val readingMode = state.readingMode
    var showDateDialog by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val sttState = viewModel.sttState


    //val aiEnabled by viewModel.aiEnabled.collectAsStateWithLifecycle()
    val aiState = viewModel.aiState
    var showAiSheet = aiState.showAiSheet

    LaunchedEffect(state.entry) {
        if (state.entry != null) {
            title = state.entry.title
            content = state.entry.content
            date = state.entry.createdDate
            mood = state.entry.mood
        }
    }
    LaunchedEffect(state.navigateUp) {
        if (state.navigateUp) {
            openDialog = false
            navController.navigateUp()
        }
    }
    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            viewModel.onEvent(
                DiaryDetailsEvent.ScreenOnStop(
                    DiaryEntry(
                        title = title,
                        content = content,
                        mood = mood,
                        createdDate = date
                    )
                )
            )
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MyBrainAppBar(
                title = "",
                actions = {
                    IconButton(onClick = {
                        viewModel.onEvent(DiaryDetailsEvent.ToggleReadingMode)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_read_mode),
                            contentDescription = stringResource(R.string.reading_mode),
                            modifier = Modifier.size(24.dp),
                            tint = if (readingMode) Color.Green else Color.Gray
                        )
                    }
                    if (state.entry != null) IconButton(onClick = { openDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete_entry)
                        )
                    }
                    TextButton(onClick = {
                        showDateDialog = true
                    }) {
                        Text(
                            text = date.fullDate(context),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.mood),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 10.dp)
            )
            Spacer(Modifier.height(8.dp))
            EntryMoodSection(
                currentMood = mood,
                onMoodChange = { mood = it }
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = stringResource(R.string.title)) },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                )
                GradientIconButton(
                    text = stringResource(id = R.string.question),
                    iconPainter = painterResource(id = R.drawable.ic_question),
                ) { viewModel.onEvent(DiaryDetailsEvent.Question(content)) }
                IconButton(onClick = {
                    viewModel.onEvent(DiaryDetailsEvent.Speech(content))
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = stringResource(R.string.speech_to_text),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
// 그 다음에 Content 입력 박스 등 나머지 UI

            Spacer(Modifier.height(8.dp))
            // Content 입력 박스와 AI 버튼 Row 배치
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                if (readingMode) {
                    Markdown(
                        content = content,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .padding(8.dp),
                        imageTransformer = Coil2ImageTransformerImpl,
                        colors = markdownColor(
                            linkText = Color.Blue,

                        ),
                        typography = markdownTypography(
                            text = MaterialTheme.typography.bodyMedium,
                            h1 = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            h2 = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            h3 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            h4 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            h5 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            h6 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text(text = stringResource(R.string.content)) },
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                    )
                }
            }
        }
        if (showDateDialog) DateTimeDialog(
            onDismissRequest = { showDateDialog = false },
            initialDate = date
        ) {
            date = it
            showDateDialog = false
        }
        if (openDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { openDialog = false },
                title = { Text(stringResource(R.string.delete_diary_entry_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.delete_diary_entry_confirmation_message
                        )
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.onEvent(DiaryDetailsEvent.DeleteEntry)
                        },
                    ) {
                        Text(
                            stringResource(R.string.delete_entry),
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            openDialog = false
                        }) {
                        Text(
                            stringResource(R.string.cancel),
                            color = Color.White
                        )
                    }
                }
            )
            AnimatedVisibility(
                visible = showAiSheet,
                enter = slideInVertically(
                    initialOffsetY = { it }, animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessVeryLow
                    )
                ),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(700))
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            // 바깥 클릭 시 Sheet 닫기 + 추가 로직 가능
                            viewModel.onEvent(DiaryDetailsEvent.AiResultHandled)
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AiQuestionSheet(
                        loading = aiState.loading,
                        result = aiState.result,
                        error = aiState.error?.toUserMessage(),
                        onCopyClick = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ai result", aiState.result.toString())
                            clipboard.setPrimaryClip(clip)
                            viewModel.onEvent(DiaryDetailsEvent.AiResultHandled)
                        },
                        onAddToDiaryClick = {
                            content = content + "\n\n" + aiState.result
                            viewModel.onEvent(DiaryDetailsEvent.AiResultHandled)
                        }
                    )
                }
            }
        AnimatedVisibility(
            visible = sttState.showSttDialog,
            enter = slideInVertically(
                initialOffsetY = { it }, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow

                )
            ),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(700))
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        viewModel.onEvent(DiaryDetailsEvent.DismissSttDialog)
                    }, contentAlignment = Alignment.BottomCenter
            ) {
                // 여기에 SttBottomSheet 컴포넌트 사용
                SttBottomSheet(
                    isListening = sttState.isListening,
                    recognizedText = sttState.recognizedText,
                    error = sttState.error,
                    onStopClick = {
                        content = content+ "\n" + sttState.recognizedText
                        viewModel.onEvent(DiaryDetailsEvent.StopSpeech) },
                )
            }
        }

        }
    }


@Composable
fun EntryMoodSection(
    currentMood: Mood,
    onMoodChange: (Mood) -> Unit
) {
    val moods = listOf(Mood.AWESOME, Mood.GOOD, Mood.OKAY, Mood.BAD, Mood.TERRIBLE)
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        moods.forEach { mood ->
            MoodItem(
                mood = mood,
                chosen = mood == currentMood,
                onMoodChange = { onMoodChange(mood) }
            )
        }
    }
}

@Composable
private fun MoodItem(mood: Mood, chosen: Boolean, onMoodChange: () -> Unit) {
    Box(Modifier.clip(RoundedCornerShape(8.dp))) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onMoodChange() }
                .padding(6.dp)
        ) {
            Icon(
                painter = painterResource(id = mood.iconRes),
                contentDescription = stringResource(mood.titleRes),
                tint = if (chosen) mood.color else Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(mood.titleRes),
                color = if (chosen) mood.color else Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}