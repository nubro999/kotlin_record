// com.mhss.app.data 패키지에 생성
package com.mhss.app.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import com.mhss.app.domain.model.SpeechRecognitionState
import com.mhss.app.domain.repository.SpeechToTextRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
@Single
class SpeechToTextRepositoryImpl(
    private val context: Context
) : SpeechToTextRepository {

    private var speechRecognizer: SpeechRecognizer? = null

    override fun startListening(language: String): Flow<SpeechRecognitionState> = callbackFlow {
        // SpeechRecognizer 관련 작업은 모두 메인 스레드에서 수행
        withContext(Dispatchers.Main) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                trySend(SpeechRecognitionState.Error("마이크 사용 권한이 필요합니다."))
                close()
                return@withContext
            }

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                trySend(SpeechRecognitionState.Error("음성 인식 서비스를 사용할 수 없습니다."))
                close()
                return@withContext
            }
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                trySend(SpeechRecognitionState.Error("음성 인식 서비스를 사용할 수 없습니다."))
                close()
                return@withContext
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        trySend(SpeechRecognitionState.Listening)
                    }

                    override fun onBeginningOfSpeech() {
                        // 이미 Listening 상태를 보냈으므로 여기서는 아무것도 하지 않음
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // 소리 크기 변화 - 필요시 UI에 반영할 수 있음
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        // 버퍼 수신 (일반적으로 사용되지 않음)
                    }

                    override fun onEndOfSpeech() {
                        // 사용자가 말을 멈추면 호출됨
                        trySend(SpeechRecognitionState.Processing("처리 중..."))
                    }

                    override fun onError(error: Int) {
                        val errorMessage = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "오디오 녹음 오류"
                            SpeechRecognizer.ERROR_CLIENT -> "클라이언트 오류"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 부족"
                            SpeechRecognizer.ERROR_NETWORK -> "네트워크 오류"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 시간 초과"
                            SpeechRecognizer.ERROR_NO_MATCH -> "음성이 인식되지 않았습니다"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성 인식기가 사용 중입니다"
                            SpeechRecognizer.ERROR_SERVER -> "서버 오류"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간이 초과되었습니다"
                            else -> "알 수 없는 오류 발생"
                        }
                        trySend(SpeechRecognitionState.Error(errorMessage))
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                        // 로그 추가로 실제 값 확인
                        Log.d("SpeechRecognition", "인식된 결과: $matches")

                        if (!matches.isNullOrEmpty()) {
                            // 인식된 텍스트가 있으면 성공 상태 전송
                            val recognizedText = matches[0]
                            Log.d("SpeechRecognition", "인식 성공: $recognizedText")
                            trySend(SpeechRecognitionState.Success(recognizedText))
                        }
                        // 결과가 없는 경우 에러 메시지를 보내지 않고 종료
                        // else 블록 자체를 제거
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            trySend(SpeechRecognitionState.Processing(matches[0]))
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {
                        // 추가 이벤트 - 일반적으로 사용되지 않음
                    }
                })
            }

            // Intent 설정 및 음성 인식 시작도 메인 스레드에서 실행
            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
            }

            // 음성 인식 시작
            speechRecognizer?.startListening(recognizerIntent)
        }

        // Flow가 취소되면 음성 인식 중지
        awaitClose {
            stopListening()
        }
    }

    override fun stopListening() {
        // SpeechRecognizer 관련 작업은 메인 스레드에서 수행해야 함
        val recognizer = speechRecognizer
        if (recognizer != null) {
            MainScope().launch {
                recognizer.stopListening()
                recognizer.destroy()
                speechRecognizer = null
            }
        }
    }
}

