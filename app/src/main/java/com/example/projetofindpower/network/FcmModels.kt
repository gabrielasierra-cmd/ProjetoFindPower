package com.example.projetofindpower.network

data class FcmRequest(
    val message: Message
)

data class Message(
    val topic: String? = null,
    val token: String? = null,
    val notification: FcmNotification
)

data class FcmNotification(
    val title: String,
    val body: String
)
