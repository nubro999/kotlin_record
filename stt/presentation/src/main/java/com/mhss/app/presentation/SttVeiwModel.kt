package com.mhss.app.presentation


import androidx.lifecycle.ViewModel
import com.mhss.app.domain.use_case.StartSpeechRecognitionUseCase
import com.mhss.app.domain.use_case.StopSpeechRecognitionUseCase
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SttViewModel(
    private val speechToTextUseCase: StartSpeechRecognitionUseCase,
    private val stopSpeechRecognitionUseCase: StopSpeechRecognitionUseCase,
    // 필요한 다른 usecase들
) : ViewModel() {
    // 뷰모델 구현 내용
    data class SpeechRecognitionState(
        private val isListening: Boolean = false,
        private val recognizedText: String = "",
        private val error: String? = null,
        private val showSttDialog: Boolean = false)
}