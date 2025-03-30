package com.mhss.app.data.model.openai

import com.mhss.app.data.NetworkConstants.OPENAI_MESSAGE_MODEL_TYPE
import com.mhss.app.data.NetworkConstants.OPENAI_MESSAGE_USER_TYPE
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageType
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable// Kotlin의 kotlinx.serialization 라이브러리를 사용하여 JSON 직렬화/역직렬화를 가능하게 합니다.

data class OpenaiResponse( //응답 형식 변환
    val choices: List<OpenaiChoice>? = null,
    val error: OpenaiError? = null
)

@Serializable
data class OpenaiChoice(
    val message: OpenaiMessage
)

@Serializable
data class OpenaiError(
    val message: String
)

fun OpenaiMessage.toAiMessage() = AiMessage( //OpenAI 메시지를 앱에서 사용하는 메시지 형식으로 변환
    content = content,
    type = if (role == OPENAI_MESSAGE_USER_TYPE) AiMessageType.USER else AiMessageType.MODEL,
    time = Clock.System.now().toEpochMilliseconds()
)

val AiMessageType.openaiRole //앱의 메시지 타입을 OpenAI가 이해할 수 있는 형식으로 변환.
    get() = if (this == AiMessageType.USER) OPENAI_MESSAGE_USER_TYPE else OPENAI_MESSAGE_MODEL_TYPE