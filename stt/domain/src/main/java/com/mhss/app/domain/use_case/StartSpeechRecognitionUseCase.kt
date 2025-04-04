package com.mhss.app.domain.use_case


import com.mhss.app.domain.repository.SpeechRecognitionState
import com.mhss.app.domain.repository.SpeechToTextRepository
import kotlinx.coroutines.flow.Flow

class StartSpeechRecognitionUseCase(
    private val speechToTextRepository: SpeechToTextRepository
) {
    operator fun invoke(language: String = "ko-KR"): Flow<SpeechRecognitionState> {
        return speechToTextRepository.startListening(language)
    }
}
