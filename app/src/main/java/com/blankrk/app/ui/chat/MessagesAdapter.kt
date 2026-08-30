package com.blankrk.app.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankrk.app.data.model.ChatMessage
import com.blankrk.app.databinding.ItemMessageReceivedBinding
import com.blankrk.app.databinding.ItemMessageSentBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesAdapter(
    private val onAudioClicked: (ChatMessage) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ChatMessage>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    fun submitList(newItems: List<ChatMessage>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].senderId == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = items[position]
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))

        when (holder) {
            is SentViewHolder -> holder.bind(message, timeStr)
            is ReceivedViewHolder -> holder.bind(message, timeStr)
        }
    }

    override fun getItemCount() = items.size

    inner class SentViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage, timeStr: String) {
            renderContent(
                message, timeStr,
                binding.messageText, binding.messageImage, binding.audioContainer,
                binding.audioDuration, binding.messageTime, binding.btnPlayAudio
            )
        }
    }

    inner class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage, timeStr: String) {
            renderContent(
                message, timeStr,
                binding.messageText, binding.messageImage, binding.audioContainer,
                binding.audioDuration, binding.messageTime, binding.btnPlayAudio
            )
        }
    }

    private fun renderContent(
        message: ChatMessage, timeStr: String,
        textView: android.widget.TextView,
        imageView: android.widget.ImageView,
        audioContainer: View,
        audioDurationView: android.widget.TextView,
        timeView: android.widget.TextView,
        playButton: View
    ) {
        textView.visibility = View.GONE
        imageView.visibility = View.GONE
        audioContainer.visibility = View.GONE

        when (message.type) {
            "text" -> {
                textView.visibility = View.VISIBLE
                textView.text = message.text
            }
            "image" -> {
                imageView.visibility = View.VISIBLE
                Glide.with(imageView).load(message.mediaUrl).into(imageView)
            }
            "audio" -> {
                audioContainer.visibility = View.VISIBLE
                val mins = message.audioDurationSeconds / 60
                val secs = message.audioDurationSeconds % 60
                audioDurationView.text = String.format("%d:%02d", mins, secs)
                playButton.setOnClickListener { onAudioClicked(message) }
            }
        }
        timeView.text = timeStr
    }
}
