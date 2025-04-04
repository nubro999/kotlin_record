package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.SpeechRecognitionState
import com.mhss.app.domain.repository.SpeechToTextRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class SpeechToTextUseCase(
    private val repository: SpeechToTextRepository
) {
    operator fun invoke(language: String = "ko-KR"): Flow<SpeechRecognitionState> {
        return repository.startListening(language)
    }

    fun stopListening() {
        repository.stopListening()
    }

    fun isListening(): Boolean = repository.isListening
}
