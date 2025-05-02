// com.mhss.app.domain.use_case 패키지에 업데이트
package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.SpeechToTextRepository
import org.koin.core.annotation.Single

@Single
class StopSpeechRecognitionUseCase(
    private val speechToTextRepository: SpeechToTextRepository
) {
    operator fun invoke() {
        speechToTextRepository.stopListening()
    }
}
