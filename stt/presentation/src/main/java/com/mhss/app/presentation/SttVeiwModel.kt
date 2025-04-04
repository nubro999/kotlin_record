package com.mhss.app.presentation


import androidx.lifecycle.ViewModel
import com.mhss.app.domain.use_case.SpeechToTextUseCase
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SttViewModel(
    private val speechToTextUseCase: SpeechToTextUseCase
    // 필요한 다른 usecase들
) : ViewModel() {
    // 뷰모델 구현 내용
}
