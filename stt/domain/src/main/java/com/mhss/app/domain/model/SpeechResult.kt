package com.mhss.app.domain.model

sealed class SpeechResult {
    data object Ready : SpeechResult()
    data object Started : SpeechResult()
    data object Finished : SpeechResult()
    data class SoundLevel(val level: Float) : SpeechResult()
    data class PartialResult(val text: String) : SpeechResult()
    data class FinalResult(val text: String) : SpeechResult()
    data class Error(val message: String) : SpeechResult()
}


