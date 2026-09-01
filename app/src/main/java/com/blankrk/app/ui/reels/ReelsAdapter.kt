package com.blankrk.app.ui.reels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankrk.app.R
import com.blankrk.app.data.model.ShortVideo
import com.blankrk.app.databinding.ItemReelBinding
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

class ReelsAdapter(
    private val onLikeClicked: (ShortVideo, Int) -> Unit,
    private val onFollowClicked: (ShortVideo, Int) -> Unit,
    private val onShareClicked: (ShortVideo) -> Unit
) : RecyclerView.Adapter<ReelsAdapter.ReelViewHolder>() {

    private val items = mutableListOf<ShortVideo>()
    private var currentlyPlayingHolder: ReelViewHolder? = null

    fun submitList(newItems: List<ShortVideo>) {
        val startPos = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPos, newItems.size)
    }

    fun updateItem(position: Int, updated: ShortVideo) {
        items[position] = updated
        notifyItemChanged(position)
    }

    fun getItem(position: Int): ShortVideo = items[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        val binding = ItemReelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    override fun onViewAttachedToWindow(holder: ReelViewHolder) {
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: ReelViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.releasePlayer()
    }

    // Called by fragment when ViewPager2 page changes — plays only the visible reel
    fun playVideoAt(recyclerView: RecyclerView, position: Int) {
        currentlyPlayingHolder?.pausePlayback()
        val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ReelViewHolder
        holder?.startPlayback()
        currentlyPlayingHolder = holder
    }

    inner class ReelViewHolder(private val binding: ItemReelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var youTubePlayer: YouTubePlayer? = null
        private var isPlayerInitialized = false

        fun bind(item: ShortVideo, position: Int) {
            binding.channelTitle.text = "@${item.channelTitle}"
            binding.videoCaption.text = item.title
            binding.likeCount.text = formatCount(item.likeCount)
            binding.commentCount.text = formatCount(item.commentCount)

            binding.btnLike.setImageResource(
                if (item.isLikedByUser) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
            binding.btnFollow.text = if (item.isFollowingChannel)
                binding.root.context.getString(R.string.following)
            else
                binding.root.context.getString(R.string.follow)

            Glide.with(binding.channelAvatar)
                .load(item.thumbnailUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(binding.channelAvatar)

            binding.btnLike.setOnClickListener { onLikeClicked(item, position) }
            binding.btnFollow.setOnClickListener { onFollowClicked(item, position) }
            binding.btnShare.setOnClickListener { onShareClicked(item) }

            setupPlayer(item.videoId)
        }

        private fun setupPlayer(videoId: String) {
            if (isPlayerInitialized) {
                youTubePlayer?.cueVideo(videoId, 0f)
                return
            }

            binding.youtubePlayerView.let { playerView ->
                (binding.root.context as? androidx.lifecycle.LifecycleOwner)?.lifecycle
                    ?.addObserver(playerView)

                // Chromeless — no YouTube controls, no branding UI, no related videos
                val iframeOptions = IFramePlayerOptions.Builder()
                    .controls(0)
                    .rel(0)
                    .ivLoadPolicy(3) // hide video annotations
                    .ccLoadPolicy(0)
                    .fullscreen(0)
                    .build()

                playerView.initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(player: YouTubePlayer) {
                        youTubePlayer = player
                        isPlayerInitialized = true
                        player.cueVideo(videoId, 0f)
                    }
                }, iframeOptions)
            }
        }

        fun startPlayback() {
            youTubePlayer?.play()
        }

        fun pausePlayback() {
            youTubePlayer?.pause()
        }

        fun releasePlayer() {
            binding.youtubePlayerView.release()
        }

        private fun formatCount(count: Long): String {
            return when {
                count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
                count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
                else -> count.toString()
            }
        }
    }
}
