package com.blankrk.app.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankrk.app.data.repository.ChatRepository
import com.blankrk.app.databinding.ActivityChatBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val repository = ChatRepository()
    private lateinit var adapter: MessagesAdapter

    private var otherUserId = ""
    private var otherUserName = ""

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var mediaPlayer: MediaPlayer? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sendImage(it) }
    }

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) toggleAudioRecording()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        otherUserId = intent.getStringExtra("otherUserId") ?: ""
        otherUserName = intent.getStringExtra("otherUserName") ?: "User"
        binding.chatHeaderName.text = otherUserName

        adapter = MessagesAdapter { message -> playAudio(message.mediaUrl) }
        binding.messagesRecycler.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.messagesRecycler.adapter = adapter

        val chatId = repository.chatIdFor(otherUserId)
        repository.listenToMessages(chatId) { messages ->
            adapter.submitList(messages)
            binding.messagesRecycler.scrollToPosition(messages.size - 1)
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnPlus.setOnClickListener {
            binding.attachmentRow.visibility =
                if (binding.attachmentRow.visibility == android.view.View.VISIBLE)
                    android.view.View.GONE else android.view.View.VISIBLE
        }

        binding.btnAttachImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
            binding.attachmentRow.visibility = android.view.View.GONE
        }

        binding.btnAttachAudio.setOnClickListener {
            requestAudioPermissionAndRecord()
            binding.attachmentRow.visibility = android.view.View.GONE
        }

        binding.btnSend.setOnClickListener {
            val text = binding.messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                lifecycleScope.launch {
                    repository.sendTextMessage(otherUserId, otherUserName, text)
                }
                binding.messageInput.text.clear()
            }
        }
    }

    private fun sendImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                repository.sendMediaMessage(otherUserId, otherUserName, bytes, "image")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun requestAudioPermissionAndRecord() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            toggleAudioRecording()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private var recordStartTime = 0L

    private fun toggleAudioRecording() {
        if (!isRecording) {
            audioFile = File(cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            recordStartTime = System.currentTimeMillis()
        } else {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            val durationSec = ((System.currentTimeMillis() - recordStartTime) / 1000).toInt()

            audioFile?.let { file ->
                lifecycleScope.launch {
                    try {
                        val bytes = file.readBytes()
                        repository.sendMediaMessage(
                            otherUserId, otherUserName, bytes, "audio", durationSec
                        )
                        file.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun playAudio(url: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener { start() }
            prepareAsync()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaRecorder?.release()
    }
}
