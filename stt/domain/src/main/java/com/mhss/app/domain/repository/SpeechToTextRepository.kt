// com.mhss.app.domain.repository 패키지에 생성
package com.mhss.app.domain.repository

import com.mhss.app.domain.model.SpeechRecognitionState
import kotlinx.coroutines.flow.Flow

interface SpeechToTextRepository {
    fun startListening(language: String): Flow<SpeechRecognitionState>
    fun stopListening()
}
