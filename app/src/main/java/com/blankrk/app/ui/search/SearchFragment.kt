package com.blankrk.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankrk.app.BuildConfig
import com.blankrk.app.databinding.FragmentSearchBinding
import com.blankrk.app.data.network.RetrofitClient
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SearchAdapter { item ->
            // TODO: open this video in a single-reel player screen (Part 4 me add karenge)
        }
        binding.searchResultsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResultsRecycler.adapter = adapter

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.searchInput.text.toString())
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        binding.searchLoadingSpinner.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.youTubeApiService.searchShorts(
                    q = query,
                    apiKey = BuildConfig.YOUTUBE_API_KEY
                )
                val results = response.items.map {
                    com.blankrk.app.data.model.ShortVideo(
                        videoId = it.id.videoId,
                        title = it.snippet.title,
                        channelId = it.snippet.channelId,
                        channelTitle = it.snippet.channelTitle,
                        thumbnailUrl = it.snippet.thumbnails.high?.url
                            ?: it.snippet.thumbnails.medium?.url ?: ""
                    )
                }
                adapter.submitList(results)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                binding.searchLoadingSpinner.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
