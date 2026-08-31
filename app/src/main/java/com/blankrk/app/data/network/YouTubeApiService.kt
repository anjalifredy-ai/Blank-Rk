package com.blankrk.app.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("search")
    suspend fun searchShorts(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoDuration") videoDuration: String = "short",
        @Query("maxResults") maxResults: Int = 15,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    @GET("videos")
    suspend fun getVideoStats(
        @Query("part") part: String = "statistics",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): YouTubeVideoStatsResponse

    @GET("channels")
    suspend fun getChannelDetails(
        @Query("part") part: String = "snippet",
        @Query("id") channelId: String,
        @Query("key") apiKey: String
    ): YouTubeChannelResponse
}
