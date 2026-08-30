package com.blankrk.app.data.model

data class ChatPreview(
    val chatId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String = "",
    val lastMessage: String = "",
    val lastMessageType: String = "text", // text, image, audio
    val timestamp: Long = 0
)

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val type: String = "text", // text, image, audio
    val text: String = "",
    val mediaUrl: String = "",
    val audioDurationSeconds: Int = 0,
    val timestamp: Long = 0,
    val seen: Boolean = false
)
