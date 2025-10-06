package com.example.fcm_learning

data class ChatState (
    val isEnteringToken : Boolean = true,
    val remoteToken: String = "",
    val messageText: String = ""
)