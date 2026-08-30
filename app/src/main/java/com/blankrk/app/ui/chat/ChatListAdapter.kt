package com.blankrk.app.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankrk.app.data.model.ChatPreview
import com.blankrk.app.databinding.ItemChatPreviewBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val onChatClicked: (ChatPreview) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val items = mutableListOf<ChatPreview>()

    fun submitList(newItems: List<ChatPreview>) {
        items.clear()
        items.addAll(newItems.sortedByDescending { it.timestamp })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ChatViewHolder(private val binding: ItemChatPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatPreview) {
            binding.chatUserName.text = item.otherUserName
            binding.chatLastMessage.text = when (item.lastMessageType) {
                "image" -> "📷 Photo"
                "audio" -> "🎤 Voice message"
                else -> item.lastMessage
            }
            if (item.timestamp > 0) {
                binding.chatTimestamp.text = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(Date(item.timestamp))
            }
            Glide.with(binding.chatAvatar).load(item.otherUserAvatar)
                .placeholder(com.blankrk.app.R.drawable.ic_profile_placeholder)
                .into(binding.chatAvatar)

            binding.root.setOnClickListener { onChatClicked(item) }
        }
    }
}
