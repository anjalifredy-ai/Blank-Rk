package com.blankrk.app.ui.reels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blankrk.app.data.repository.ReelsRepository
import com.blankrk.app.databinding.FragmentReelsBinding
import kotlinx.coroutines.launch

class ReelsFragment : Fragment() {

    private var _binding: FragmentReelsBinding? = null
    private val binding get() = _binding!!

    private val repository = ReelsRepository()
    private lateinit var adapter: ReelsAdapter

    private var nextPageToken: String? = null
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReelsAdapter(
            onLikeClicked = { item, position -> handleLike(item, position) },
            onFollowClicked = { item, position -> handleFollow(item, position) },
            onShareClicked = { item -> handleShare(item) }
        )

        binding.reelsViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.reelsViewPager.adapter = adapter

        binding.reelsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val recyclerView = binding.reelsViewPager.getChildAt(0) as RecyclerView
                adapter.playVideoAt(recyclerView, position)

                // Load more when near the end (infinite scroll)
                if (position >= adapter.itemCount - 3) {
                    loadShorts()
                }
            }
        })

        loadShorts()
    }

    private fun loadShorts() {
        if (isLoading) return
        isLoading = true
        binding.loadingSpinner.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val (shorts, token) = repository.fetchShorts(nextPageToken)
                adapter.submitList(shorts)
                nextPageToken = token

                // Autoplay first video on very first load
                if (adapter.itemCount == shorts.size && shorts.isNotEmpty()) {
                    binding.reelsViewPager.post {
                        val recyclerView = binding.reelsViewPager.getChildAt(0) as RecyclerView
                        adapter.playVideoAt(recyclerView, 0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
                binding.loadingSpinner.visibility = View.GONE
            }
        }
    }

    private fun handleLike(item: com.blankrk.app.data.model.ShortVideo, position: Int) {
        val newState = !item.isLikedByUser
        val updated = item.copy(
            isLikedByUser = newState,
            likeCount = if (newState) item.likeCount + 1 else item.likeCount - 1
        )
        adapter.updateItem(position, updated)

        viewLifecycleOwner.lifecycleScope.launch {
            repository.toggleLike(item.videoId, item.isLikedByUser)
        }
    }

    private fun handleFollow(item: com.blankrk.app.data.model.ShortVideo, position: Int) {
        val newState = !item.isFollowingChannel
        val updated = item.copy(isFollowingChannel = newState)
        adapter.updateItem(position, updated)

        viewLifecycleOwner.lifecycleScope.launch {
            repository.toggleFollow(item.channelId, item.channelTitle, item.isFollowingChannel)
        }
    }

    private fun handleShare(item: com.blankrk.app.data.model.ShortVideo) {
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, "https://youtube.com/shorts/${item.videoId}")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
