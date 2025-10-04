package com.example.carepick.ui.selfDiagnosis.model

sealed class ChatMessage {
    data class User(val text: String) : ChatMessage()
    data class Bot(val text: String) : ChatMessage()
    data object Typing : ChatMessage()

    data class SystemSpecialtyButtons(val specialties: List<String>) : ChatMessage()
}