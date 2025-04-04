package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.SpeechToTextRepository

class StopSpeechRecognitionUseCase(
    private val speechToTextRepository: SpeechToTextRepository
) {
    operator fun invoke() {
        speechToTextRepository.stopListening()
    }
}
