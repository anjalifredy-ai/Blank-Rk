package com.blankrk.app.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankrk.app.data.repository.ChatRepository
import com.blankrk.app.databinding.FragmentChatListBinding

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatListAdapter
    private val repository = ChatRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatListAdapter { chatPreview ->
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("otherUserId", chatPreview.otherUserId)
                putExtra("otherUserName", chatPreview.otherUserName)
            }
            startActivity(intent)
        }

        binding.chatListRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.chatListRecycler.adapter = adapter

        repository.listenToChatList { chats -> adapter.submitList(chats) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
