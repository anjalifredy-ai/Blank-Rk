package com.blankrk.app.data.repository

import com.blankrk.app.data.model.ChatMessage
import com.blankrk.app.data.model.ChatPreview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository {

    private val db = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun chatIdFor(otherUserId: String): String {
        return if (currentUserId < otherUserId) "${currentUserId}_$otherUserId"
        else "${otherUserId}_$currentUserId"
    }

    fun listenToChatList(onUpdate: (List<ChatPreview>) -> Unit) {
        db.child("userChats").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val previews = snapshot.children.mapNotNull {
                        it.getValue(ChatPreview::class.java)
                    }
                    onUpdate(previews)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun listenToMessages(chatId: String, onUpdate: (List<ChatMessage>) -> Unit) {
        db.child("chats").child(chatId).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull {
                        it.getValue(ChatMessage::class.java)
                    }
                    onUpdate(messages.sortedBy { it.timestamp })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    suspend fun sendTextMessage(otherUserId: String, otherUserName: String, text: String) {
        val chatId = chatIdFor(otherUserId)
        val messageId = db.child("chats").child(chatId).child("messages").push().key
            ?: UUID.randomUUID().toString()

        val message = ChatMessage(
            messageId = messageId,
            senderId = currentUserId,
            type = "text",
            text = text,
            timestamp = System.currentTimeMillis()
        )

        db.child("chats").child(chatId).child("messages").child(messageId)
            .setValue(message).await()

        updateChatPreview(chatId, otherUserId, otherUserName, text, "text")
    }

    suspend fun sendMediaMessage(
        otherUserId: String,
        otherUserName: String,
        fileBytes: ByteArray,
        type: String, // "image" or "audio"
        audioDurationSeconds: Int = 0
    ) {
        val chatId = chatIdFor(otherUserId)
        val messageId = db.child("chats").child(chatId).child("messages").push().key
            ?: UUID.randomUUID().toString()

        val extension = if (type == "image") "jpg" else "m4a"
        val mediaRef = storage.child("chatMedia/$chatId/$messageId.$extension")
        mediaRef.putBytes(fileBytes).await()
        val downloadUrl = mediaRef.downloadUrl.await().toString()

        val message = ChatMessage(
            messageId = messageId,
            senderId = currentUserId,
            type = type,
            mediaUrl = downloadUrl,
            audioDurationSeconds = audioDurationSeconds,
            timestamp = System.currentTimeMillis()
        )

        db.child("chats").child(chatId).child("messages").child(messageId)
            .setValue(message).await()

        val previewText = if (type == "image") "Photo" else "Voice message"
        updateChatPreview(chatId, otherUserId, otherUserName, previewText, type)
    }

    private suspend fun updateChatPreview(
        chatId: String, otherUserId: String, otherUserName: String,
        lastMessage: String, type: String
    ) {
        val timestamp = System.currentTimeMillis()

        val myPreview = ChatPreview(
            chatId = chatId, otherUserId = otherUserId, otherUserName = otherUserName,
            lastMessage = lastMessage, lastMessageType = type, timestamp = timestamp
        )
        db.child("userChats").child(currentUserId).child(chatId).setValue(myPreview).await()

        val myName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"
        val theirPreview = ChatPreview(
            chatId = chatId, otherUserId = currentUserId, otherUserName = myName,
            lastMessage = lastMessage, lastMessageType = type, timestamp = timestamp
        )
        db.child("userChats").child(otherUserId).child(chatId).setValue(theirPreview).await()
    }
}
