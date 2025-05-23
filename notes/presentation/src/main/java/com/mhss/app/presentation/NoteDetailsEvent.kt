package com.mhss.app.presentation

import com.mhss.app.domain.model.Note

sealed class NoteDetailsEvent {
    data class DeleteNote(val note: Note) : NoteDetailsEvent()
    data object ToggleReadingMode : NoteDetailsEvent()
    data class Summarize(val content: String): NoteDetailsEvent(), AiAction
    data class AutoFormat(val content: String): NoteDetailsEvent(), AiAction
    data class CorrectSpelling(val content: String): NoteDetailsEvent(), AiAction
    data object AiResultHandled: NoteDetailsEvent()
    data class ScreenOnStop(val currentNote: Note): NoteDetailsEvent()
    data class Speech(val content: String): NoteDetailsEvent(), SpeechEvent
    data object DismissSttDialog: NoteDetailsEvent(), SpeechEvent
    data object StopSpeech: NoteDetailsEvent(), SpeechEvent
}

sealed interface AiAction
sealed interface SpeechEvent