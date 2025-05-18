package com.mhss.app.presentation

import com.mhss.app.domain.model.DiaryEntry

sealed class DiaryDetailsEvent {
    data object DeleteEntry : DiaryDetailsEvent()
    data object ToggleReadingMode : DiaryDetailsEvent()
    data class ScreenOnStop(val currentEntry: DiaryEntry) : DiaryDetailsEvent()
    data class Question(val content: String): DiaryDetailsEvent(), QuestionAction
    data object AiResultHandled: DiaryDetailsEvent()
    data class Speech(val content: String): DiaryDetailsEvent(), SpeechEventDiary
    data object DismissSttDialog: DiaryDetailsEvent(), SpeechEventDiary
    data object StopSpeech: DiaryDetailsEvent(), SpeechEventDiary
}

sealed interface QuestionAction
sealed interface SpeechEventDiary