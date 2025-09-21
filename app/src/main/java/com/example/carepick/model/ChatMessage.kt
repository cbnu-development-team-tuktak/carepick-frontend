package com.example.carepick.model

sealed class ChatMessage {
    data class User(val text: String) : ChatMessage()
    data class Bot(val text: String) : ChatMessage()
    data object Typing : ChatMessage()
}