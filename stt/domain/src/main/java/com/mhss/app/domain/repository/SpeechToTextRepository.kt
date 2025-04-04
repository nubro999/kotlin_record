package com.mhss.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SpeechToTextRepository {
    fun startListening(language: String = "ko-KR"): Flow<SpeechRecognitionState>
    fun stopListening()
    val isListening: Boolean
}

sealed class SpeechRecognitionState {
    data object Idle : SpeechRecognitionState()
    data object Listening : SpeechRecognitionState()
    data class Processing(val partialText: String = "") : SpeechRecognitionState()
    data class Success(val text: String) : SpeechRecognitionState()
    data class Error(val message: String) : SpeechRecognitionState()
}
