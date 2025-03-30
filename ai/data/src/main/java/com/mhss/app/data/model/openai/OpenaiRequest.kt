package com.mhss.app.data.model.openai

import com.mhss.app.data.NetworkConstants
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.systemMessage
import kotlinx.serialization.Serializable

@Serializable
data class OpenaiMessageRequestBody( //요청본문
    val model: String,
    val messages: List<OpenaiMessage>
)

@Serializable
data class OpenaiMessage(
    val content: String,
    val role: String
)

fun List<AiMessage>.toOpenAiRequestBody(
    model: String,
): OpenaiMessageRequestBody {
    return OpenaiMessageRequestBody(
        model = model,
        messages =
        listOf(
            OpenaiMessage(
                content = systemMessage, //항상 시스템메세지가 포함
                role = NetworkConstants.OPENAI_MESSAGE_SYSTEM_TYPE
            )
        ) + map {
            OpenaiMessage(
                content = it.content + it.attachmentsText, //ai응답과 사용자 응답을 구별
                role = it.type.openaiRole
            )
        }
    )
}