package com.blankrk.app.ui.following

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blankrk.app.data.model.ShortVideo
import com.blankrk.app.databinding.FragmentFollowingBinding
import com.blankrk.app.data.network.RetrofitClient
import com.blankrk.app.ui.reels.ReelsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FollowingFragment : Fragment() {

    private var _binding: FragmentFollowingBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ReelsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReelsAdapter(
            onLikeClicked = { _, _ -> },
            onFollowClicked = { _, _ -> },
            onShareClicked = { }
        )
        binding.followingViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.followingViewPager.adapter = adapter

        binding.followingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val recyclerView = binding.followingViewPager.getChildAt(0) as RecyclerView
                adapter.playVideoAt(recyclerView, position)
            }
        })

        loadFollowedChannelsVideos()
    }

    private fun loadFollowedChannelsVideos() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val followingSnapshot = FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .collection("following").get().await()

                val channelIds = followingSnapshot.documents.map { it.id }

                if (channelIds.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    return@launch
                }

                val allVideos = mutableListOf<ShortVideo>()
                for (channelId in channelIds.take(10)) { // quota safety
                    val response = RetrofitClient.youTubeApiService.searchShorts(
                        q = "",
                        apiKey = com.blankrk.app.BuildConfig.YOUTUBE_API_KEY
                    )
                    // Note: proper channel-specific fetch uses channelId param;
                    // refined in a later pass once upload feature ships.
                    allVideos.addAll(response.items.filter { it.snippet.channelId == channelId }
                        .map {
                            ShortVideo(
                                videoId = it.id.videoId,
                                title = it.snippet.title,
                                channelId = it.snippet.channelId,
                                channelTitle = it.snippet.channelTitle,
                                thumbnailUrl = it.snippet.thumbnails.high?.url ?: "",
                                isFollowingChannel = true
                            )
                        })
                }

                if (allVideos.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                } else {
                    adapter.submitList(allVideos)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
