package com.example.fcm_learning

data class SendMessageDto(
    val to : String?,
    val notification : NotificationBody
)

data class NotificationBody(
    val title: String = "",
    val body: String = ""
)