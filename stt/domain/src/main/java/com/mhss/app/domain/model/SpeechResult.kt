package com.mhss.app.domain.model

sealed class SpeechResult {
    object Ready : SpeechResult()
    object Started : SpeechResult()
    object Finished : SpeechResult()
    data class SoundLevel(val level: Float) : SpeechResult()
    data class PartialResult(val text: String) : SpeechResult()
    data class FinalResult(val text: String) : SpeechResult()
    data class Error(val message: String) : SpeechResult()
}


