package com.mhss.app.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi
import com.mhss.app.domain.model.SpeechResult
import com.mhss.app.domain.repository.SpeechRecognitionState
import com.mhss.app.domain.repository.SpeechToTextRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.annotation.Factory

@Factory
class SpeechToTextRepositoryImpl(
    private val context: Context
) : SpeechToTextRepository {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    override val isListening: Boolean get() = _isListening.value

    override fun startListening(language: String): Flow<SpeechRecognitionState> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            trySend(SpeechRecognitionState.Error("음성 인식을 사용할 수 없습니다"))
            close()
            return@callbackFlow
        }

        // 기존 인스턴스 정리
        speechRecognizer?.destroy()

        // 새 인스턴스 생성
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    trySend(SpeechRecognitionState.Listening)
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val results = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!results.isNullOrEmpty()) {
                        trySend(SpeechRecognitionState.Processing(results[0]))
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val speechResults = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!speechResults.isNullOrEmpty()) {
                        trySend(SpeechRecognitionState.Success(speechResults[0]))
                    } else {
                        trySend(SpeechRecognitionState.Error("인식 결과가 없습니다"))
                    }
                    trySend(SpeechRecognitionState.Idle)
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    val errorMessage = when(error) {
                        SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                        SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 없음"
                        SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 타임아웃"
                        SpeechRecognizer.ERROR_NO_MATCH -> "일치하는 결과 없음"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성 인식기 사용중"
                        SpeechRecognizer.ERROR_SERVER -> "서버 에러"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 없음"
                        else -> "알 수 없는 에러"
                    }
                    trySend(SpeechRecognitionState.Error(errorMessage))
                    trySend(SpeechRecognitionState.Idle)
                }
            })
        }

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        // 음성 인식 시작
        trySend(SpeechRecognitionState.Idle) // 초기 상태 전송
        speechRecognizer?.startListening(recognizerIntent)

        awaitClose {
            stopListening()
        }
    }

    override fun stopListening() {
        _isListening.value = false
        speechRecognizer?.apply {
            stopListening()
            cancel()
            destroy()
        }
        speechRecognizer = null
    }
}

