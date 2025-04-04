package com.mhss.app.domain.model //위치는 ai

data class AiMessage( //앱의 응답
    val content: String,
    val type: AiMessageType,
    val time: Long,
    val attachments: List<AiMessageAttachment> = emptyList(),
    val attachmentsText: String = ""
)

sealed interface AiMessageAttachment {
    data class Note (val note: com.mhss.app.domain.model.Note): AiMessageAttachment //note data type
    data class Task (val task: com.mhss.app.domain.model.Task): AiMessageAttachment
    data object CalenderEvents: AiMessageAttachment
    //data class Diary (val diary: com.mhss.app.domain.model.DiaryEntry): AiMessageAttachment
}

enum class AiMessageType {
    USER,
    MODEL
}
