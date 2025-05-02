// com.mhss.app.domain.use_case 패키지에 업데이트
package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.SpeechRecognitionState
import com.mhss.app.domain.repository.SpeechToTextRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class StartSpeechRecognitionUseCase(
    private val speechToTextRepository: SpeechToTextRepository
) {
    operator fun invoke(language: String = "ko-KR"): Flow<SpeechRecognitionState> {
        return speechToTextRepository.startListening(language)
    }
}
