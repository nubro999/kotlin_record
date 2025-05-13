package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.AiConstants
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.SpeechRecognitionState
import com.mhss.app.domain.use_case.*
import com.mhss.app.util.date.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Named
import com.mhss.app.domain.questionAboutDay
import com.mhss.app.network.NetworkResult
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update


@KoinViewModel
class DiaryDetailsViewModel(
    private val getEntry: GetDiaryEntryUseCase,
    private val addEntry: AddDiaryEntryUseCase,
    private val updateEntry: UpdateDiaryEntryUseCase,
    private val deleteEntry: DeleteDiaryEntryUseCase,
    private val sendAiPrompt: SendAiPromptUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val startSpeechRecognitionUseCase: StartSpeechRecognitionUseCase,
    private val stopSpeechRecognitionUseCase: StopSpeechRecognitionUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
    entryId: Int
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set
    var sttState by mutableStateOf(STTState())
        private set

    private lateinit var aiKey: String
    private lateinit var aiModel: String
    private lateinit var openaiURL: String
    private val _aiEnabled = MutableStateFlow(false)
    val aiEnabled: StateFlow<Boolean> = _aiEnabled

    var aiState by mutableStateOf((AiState()))
        private set

    private var aiActionJob: Job? = null
    private var speechEventJob: Job? = null


    private val aiProvider = //여기서 ai관련 설정정보를 수집
        getPreference(intPreferencesKey(PrefsConstants.AI_PROVIDER_KEY), AiProvider.None.id)
            .map { id -> AiProvider.entries.first { it.id == id } }
            .onEach { provider ->
                _aiEnabled.update { provider != AiProvider.None }
                when (provider) {
                    AiProvider.OpenAI -> {
                        aiKey = getPreference(
                            stringPreferencesKey(PrefsConstants.OPENAI_KEY),
                            ""
                        ).first()
                        aiModel = getPreference(
                            stringPreferencesKey(PrefsConstants.OPENAI_MODEL_KEY),
                            AiConstants.OPENAI_DEFAULT_MODEL
                        ).first()
                        openaiURL = getPreference(
                            stringPreferencesKey(PrefsConstants.OPENAI_URL_KEY),
                            AiConstants.OPENAI_BASE_URL
                        ).first()
                    }

                    AiProvider.Gemini -> {
                        aiKey = getPreference(
                            stringPreferencesKey(PrefsConstants.GEMINI_KEY),
                            ""
                        ).first()
                        aiModel = getPreference(
                            stringPreferencesKey(PrefsConstants.GEMINI_MODEL_KEY),
                            AiConstants.GEMINI_DEFAULT_MODEL
                        ).first()
                        openaiURL = ""
                    }

                    else -> {
                        aiKey = ""
                        aiModel = ""
                        openaiURL = ""
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, AiProvider.None)

    init {
        viewModelScope.launch {
            if (entryId != -1) {
                uiState = uiState.copy(
                    entry = getEntry(entryId),
                    readingMode = true
                )
            }
        }
    }

    fun onEvent(event: DiaryDetailsEvent) {
        when (event) {
            is DiaryDetailsEvent.DeleteEntry -> viewModelScope.launch {
                deleteEntry(uiState.entry!!)
                uiState = uiState.copy(navigateUp = true)
            }

            is DiaryDetailsEvent.ToggleReadingMode -> {
                uiState = uiState.copy(readingMode = !uiState.readingMode)
            }
            is QuestionAction -> aiActionJob = viewModelScope.launch {
                val prompt = when (event) {
                    is DiaryDetailsEvent.Question -> event.content.questionAboutDay
                }
                aiState = aiState.copy(
                    loading = true,
                    showAiSheet = true,
                    result = null,
                    error = null
                )
                val result = sendAiPrompt(prompt) //여기서 ai요청?
                aiState = when (result) {
                    is NetworkResult.Success -> aiState.copy(
                        loading = false,
                        result = result.data,
                        error = null
                    ) //prompt, aistate, result

                    is NetworkResult.Failure -> aiState.copy(error = result, loading = false)
                }
            }
            DiaryDetailsEvent.AiResultHandled -> {
                aiActionJob?.cancel()
                aiActionJob = null
                aiState = aiState.copy(showAiSheet = false)
            }
            // Using applicationScope to avoid cancelling when the user exits the screen
            // and the view model is cleared before the job finishes
            is DiaryDetailsEvent.ScreenOnStop -> applicationScope.launch {
                if (!uiState.navigateUp) {
                    if (uiState.entry == null) {
                        if (event.currentEntry.title.isNotBlank() || event.currentEntry.content.isNotBlank()) {
                            val entry = event.currentEntry.copy(
                                updatedDate = now()
                            )
                            val id = addEntry(entry)
                            uiState = uiState.copy(entry = entry.copy(id = id.toInt()))
                        }
                    } else if (entryChanged(uiState.entry!!, event.currentEntry)) {
                        val newEntry = uiState.entry!!.copy(
                            title = event.currentEntry.title,
                            content = event.currentEntry.content,
                            mood = event.currentEntry.mood,
                            createdDate = event.currentEntry.createdDate,
                            updatedDate = now()
                        )
                        updateEntry(newEntry)
                        uiState = uiState.copy(entry = newEntry)
                    }
                }
            }
            is DiaryDetailsEvent.Speech -> speechEventJob = viewModelScope.launch {
                sttState = sttState.copy(
                    isListening = true,
                    recognizedText = "",
                    error = null,
                    showSttDialog = true
                )

                // 음성 인식 시작
                startSpeechRecognitionUseCase().collect { recognitionState ->
                    when (recognitionState) {
                        is SpeechRecognitionState.Listening -> {
                            sttState = sttState.copy(isListening = true)
                        }
                        is SpeechRecognitionState.Processing -> {
                            // 실시간 인식 텍스트를 상태에만 업데이트 (노트에는 저장하지 않음)
                            sttState = sttState.copy(recognizedText = recognitionState.partialText)
                        }
                        is SpeechRecognitionState.Success -> {
                            // 최종 인식 결과도 상태에만 업데이트 (노트에는 저장하지 않음)
                            sttState = sttState.copy(
                                isListening = false,
                                recognizedText = recognitionState.text
                            )
                        }
                        is SpeechRecognitionState.Error -> {
                            sttState = sttState.copy(
                                isListening = false,
                                error = recognitionState.message
                            )
                        }
                        is SpeechRecognitionState.Idle -> {
                            if (sttState.error == null) {
                                delay(1500)
                            }
                            sttState = sttState.copy(showSttDialog = false)
                        }
                    }
                }
            }


            is DiaryDetailsEvent.DismissSttDialog -> {
                sttState = sttState.copy(showSttDialog = false)
            }

            is DiaryDetailsEvent.StopSpeech -> {
                stopSpeechRecognitionUseCase()
                speechEventJob?.cancel()
                sttState = sttState.copy(isListening = false, showSttDialog = false) // showSttDialog를 false로 설정
            }
        }
    }

    private suspend fun sendAiPrompt(prompt: String) = sendAiPrompt( //여기서 onEvent에서 prompt를 받음
        prompt,
        aiKey,
        aiModel,
        aiProvider.value,
        openaiURL
    )

    private fun entryChanged(entry: DiaryEntry, newEntry: DiaryEntry): Boolean {
        return entry.title != newEntry.title ||
                entry.content != newEntry.content ||
                entry.mood != newEntry.mood ||
                entry.createdDate != newEntry.createdDate
    }
    data class UiState(
        val entry: DiaryEntry? = null,
        val navigateUp: Boolean = false,
        val readingMode: Boolean = false
    )
    data class AiState(
        val loading: Boolean = false,
        val result: String? = null,
        val error: NetworkResult.Failure? = null,
        val showAiSheet: Boolean = false,
    )
    data class STTState(
        val isListening: Boolean = false,
        val recognizedText: String = "",
        val error: String? = null,
        val showSttDialog: Boolean = false
    )
}

