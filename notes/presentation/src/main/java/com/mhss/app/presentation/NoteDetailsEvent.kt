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
    // TODO:  data class Questioning(val content: String): NoteDetailsEvent(), AiAction
    // TODO:  data class STTNote(val content: String): NoteDetailsEvent(), STTAction
}

sealed interface AiAction