package com.blankrk.app.data.repository

import com.blankrk.app.BuildConfig
import com.blankrk.app.data.model.ShortVideo
import com.blankrk.app.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReelsRepository {

    private val api = RetrofitClient.youTubeApiService
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun fetchShorts(pageToken: String? = null): Pair<List<ShortVideo>, String?> {
        val apiKey = BuildConfig.YOUTUBE_API_KEY

        val searchResponse = api.searchShorts(pageToken = pageToken, apiKey = apiKey)
        val videoIds = searchResponse.items.joinToString(",") { it.id.videoId }

        if (videoIds.isEmpty()) return Pair(emptyList(), null)

        val statsResponse = api.getVideoStats(videoIds = videoIds, apiKey = apiKey)
        val statsMap = statsResponse.items.associateBy { it.id }

        val shorts = searchResponse.items.map { item ->
            val stats = statsMap[item.id.videoId]?.statistics
            ShortVideo(
                videoId = item.id.videoId,
                title = item.snippet.title,
                channelId = item.snippet.channelId,
                channelTitle = item.snippet.channelTitle,
                thumbnailUrl = item.snippet.thumbnails.high?.url
                    ?: item.snippet.thumbnails.medium?.url ?: "",
                likeCount = stats?.likeCount?.toLongOrNull() ?: 0,
                commentCount = stats?.commentCount?.toLongOrNull() ?: 0
            )
        }

        // Attach Firestore state (is user ne like kiya hai? follow kiya hai?)
        val enrichedShorts = enrichWithUserState(shorts)

        return Pair(enrichedShorts, searchResponse.nextPageToken)
    }

    private suspend fun enrichWithUserState(shorts: List<ShortVideo>): List<ShortVideo> {
        val uid = currentUserId ?: return shorts

        return shorts.map { short ->
            try {
                val likeDoc = firestore.collection("users").document(uid)
                    .collection("likedVideos").document(short.videoId)
                    .get().await()

                val followDoc = firestore.collection("users").document(uid)
                    .collection("following").document(short.channelId)
                    .get().await()

                short.copy(
                    isLikedByUser = likeDoc.exists(),
                    isFollowingChannel = followDoc.exists()
                )
            } catch (e: Exception) {
                short
            }
        }
    }

    suspend fun toggleLike(videoId: String, isCurrentlyLiked: Boolean) {
        val uid = currentUserId ?: return
        val ref = firestore.collection("users").document(uid)
            .collection("likedVideos").document(videoId)

        if (isCurrentlyLiked) {
            ref.delete().await()
        } else {
            ref.set(mapOf("likedAt" to System.currentTimeMillis())).await()
        }
    }

    suspend fun toggleFollow(channelId: String, channelTitle: String, isCurrentlyFollowing: Boolean) {
        val uid = currentUserId ?: return
        val ref = firestore.collection("users").document(uid)
            .collection("following").document(channelId)

        if (isCurrentlyFollowing) {
            ref.delete().await()
        } else {
            ref.set(
                mapOf(
                    "channelTitle" to channelTitle,
                    "followedAt" to System.currentTimeMillis()
                )
            ).await()
        }
    }
}
