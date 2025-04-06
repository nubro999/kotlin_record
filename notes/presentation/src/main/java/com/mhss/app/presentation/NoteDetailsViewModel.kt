package com.mhss.app.presentation

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.AiConstants
import com.mhss.app.domain.autoFormatNotePrompt
import com.mhss.app.domain.correctSpellingNotePrompt
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.model.SpeechRecognitionState
import com.mhss.app.domain.summarizeNotePrompt
import com.mhss.app.domain.use_case.AddNoteUseCase
import com.mhss.app.domain.use_case.DeleteNoteUseCase
import com.mhss.app.domain.use_case.GetAllNoteFoldersUseCase
import com.mhss.app.domain.use_case.GetNoteFolderUseCase
import com.mhss.app.domain.use_case.GetNoteUseCase
import com.mhss.app.domain.use_case.SendAiPromptUseCase
import com.mhss.app.domain.use_case.StartSpeechRecognitionUseCase
import com.mhss.app.domain.use_case.StopSpeechRecognitionUseCase
import com.mhss.app.domain.use_case.UpdateNoteUseCase
import com.mhss.app.network.NetworkResult
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.util.date.now
import kotlinx.coroutines.CoroutineScope
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
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class NoteDetailsViewModel(
    private val getNote: GetNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val addNote: AddNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val getAllFolders: GetAllNoteFoldersUseCase,
    private val getNoteFolder: GetNoteFolderUseCase,
    private val sendAiPrompt: SendAiPromptUseCase,
    private val _recognizedTextFlow: MutableStateFlow<String> = MutableStateFlow(""),
    private val startSpeechRecognitionUseCase: StartSpeechRecognitionUseCase,
    private val stopSpeechRecognitionUseCase: StopSpeechRecognitionUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
    id: Int,
    folderId: Int,
) : ViewModel() {

    var noteUiState by mutableStateOf((UiState()))
        private set

    private lateinit var aiKey: String
    private lateinit var aiModel: String
    private lateinit var openaiURL: String
    private val _aiEnabled = MutableStateFlow(false)
    val aiEnabled: StateFlow<Boolean> = _aiEnabled
    var aiState by mutableStateOf((AiState()))
        private set
    private var aiActionJob: Job? = null

    var sttState by mutableStateOf(STTState())
        private set

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
            val note: Note? = if (id != -1) getNote(id) else null
            val folder = getNoteFolder(note?.folderId ?: folderId)
            val folders = getAllFolders().first()
            noteUiState = noteUiState.copy(
                note = note,
                folder = folder,
                folders = folders,
                readingMode = note != null
            )
        }
    }

    fun onEvent(event: NoteDetailsEvent) {
        when (event) {
            // Using applicationScope to avoid cancelling when the user exits the screen
            // and the view model is cleared before the job finishes
            is NoteDetailsEvent.ScreenOnStop -> applicationScope.launch {
                if (!noteUiState.navigateUp) {
                    if (noteUiState.note == null) {
                        if (event.currentNote.title.isNotBlank() || event.currentNote.content.isNotBlank()) {
                            val note = event.currentNote.copy(
                                createdDate = now(),
                                updatedDate = now()
                            )
                            val id = addNote(note)
                            noteUiState = noteUiState.copy(note = note.copy(id = id.toInt()))
                        }
                    } else if (noteChanged(noteUiState.note!!, event.currentNote)) {
                        val newNote = noteUiState.note!!.copy(
                            title = event.currentNote.title,
                            content = event.currentNote.content,
                            folderId = event.currentNote.folderId,
                            pinned = event.currentNote.pinned,
                            updatedDate = now()
                        )
                        updateNote(newNote)
                        noteUiState = noteUiState.copy(note = newNote)
                    }
                }
            }

            is NoteDetailsEvent.DeleteNote -> viewModelScope.launch {
                deleteNote(event.note)
                noteUiState = noteUiState.copy(navigateUp = true)
            }

            NoteDetailsEvent.ToggleReadingMode -> noteUiState =
                noteUiState.copy(readingMode = !noteUiState.readingMode)

            is AiAction -> aiActionJob = viewModelScope.launch {
                val prompt = when (event) {
                    is NoteDetailsEvent.Summarize -> event.content.summarizeNotePrompt
                    is NoteDetailsEvent.AutoFormat -> event.content.autoFormatNotePrompt
                    is NoteDetailsEvent.CorrectSpelling -> event.content.correctSpellingNotePrompt
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

            NoteDetailsEvent.AiResultHandled -> {
                aiActionJob?.cancel()
                aiActionJob = null
                aiState = aiState.copy(showAiSheet = false)
            }

            is NoteDetailsEvent.Speech -> viewModelScope.launch {
                // 음성 인식 시작 전 상태 초기화
                sttState = sttState.copy(
                    isListening = true,
                    recognizedText = "",
                    error = null,
                    showSttDialog = true
                )
                // 현재 노트 내용 저장 (나중에 참조용)
                val initialContent = event.content

                // 음성 인식 시작 (도메인 레이어의 SpeechRecognitionState 사용)
                startSpeechRecognitionUseCase().collect { recognitionState ->
                    when (recognitionState) {
                        is SpeechRecognitionState.Listening -> {
                            sttState = sttState.copy(isListening = true)
                        }
                        is SpeechRecognitionState.Processing -> {
                            // 실시간 인식 텍스트 업데이트
                            sttState = sttState.copy(recognizedText = recognitionState.partialText)

                            // 실시간으로 노트 내용도 업데이트
                            updateNoteContentWithRecognizedText(initialContent, recognitionState.partialText)
                        }
                        is SpeechRecognitionState.Success -> {
                            // 최종 인식 결과로 노트 내용 업데이트
                            updateNoteContentWithRecognizedText(initialContent, recognitionState.text)

                            // 상태 업데이트
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
                            // 인식 완료 후 idle 상태로 돌아온 경우
                            if (sttState.error == null) {
                                // 성공적인 인식 후 대화상자 닫기 전 잠시 대기
                                delay(1500)
                            }
                            sttState = sttState.copy(showSttDialog = false)
                        }
                    }
                }
            }
            NoteDetailsEvent.DismissSttDialog -> {
                sttState = sttState.copy(showSttDialog = false)
            }
            NoteDetailsEvent.StopSpeech -> {
                stopSpeechRecognitionUseCase()
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

    private fun updateNoteContentWithRecognizedText(initialContent: String, recognizedText: String) {
        if (recognizedText.isEmpty()) return

        // 새로운 내용 생성 (초기 내용 + 공백 + 인식된 텍스트)
        val newContent = if (initialContent.isNotEmpty()) {
            "$initialContent $recognizedText"
        } else {
            recognizedText
        }

        // 노트 업데이트
        val updatedNote = noteUiState.note?.copy(content = newContent)
            ?: Note(
                title = "",
                content = newContent,
                folderId = noteUiState.folder?.id,
                createdDate = now(),
                updatedDate = now()
            )

        noteUiState = noteUiState.copy(note = updatedNote)
    }

        // 노트 업데이트

    private fun noteChanged(
        note: Note,
        newNote: Note,
    ): Boolean {
        return note.title != newNote.title ||
                note.content != newNote.content ||
                note.folderId != newNote.folderId ||
                note.pinned != newNote.pinned
    }

    data class UiState(
        val note: Note? = null,
        val navigateUp: Boolean = false,
        val readingMode: Boolean = false,
        val folders: List<NoteFolder> = emptyList(),
        val folder: NoteFolder? = null,
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