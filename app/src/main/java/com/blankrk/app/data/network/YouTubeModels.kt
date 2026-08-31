package com.blankrk.app.data.network

import com.google.gson.annotations.SerializedName

data class YouTubeSearchResponse(
    @SerializedName("items") val items: List<YouTubeSearchItem>,
    @SerializedName("nextPageToken") val nextPageToken: String?
)

data class YouTubeSearchItem(
    @SerializedName("id") val id: YouTubeVideoId,
    @SerializedName("snippet") val snippet: YouTubeSnippet
)

data class YouTubeVideoId(
    @SerializedName("videoId") val videoId: String?,
    @SerializedName("channelId") val channelId: String?
)

data class YouTubeSnippet(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("channelId") val channelId: String,
    @SerializedName("channelTitle") val channelTitle: String,
    @SerializedName("thumbnails") val thumbnails: YouTubeThumbnails
)

data class YouTubeThumbnails(
    @SerializedName("default") val default: YouTubeThumbnail?,
    @SerializedName("medium") val medium: YouTubeThumbnail?,
    @SerializedName("high") val high: YouTubeThumbnail?
)

data class YouTubeThumbnail(
    @SerializedName("url") val url: String
)

data class YouTubeVideoStatsResponse(
    @SerializedName("items") val items: List<YouTubeVideoStatsItem>
)

data class YouTubeVideoStatsItem(
    @SerializedName("id") val id: String,
    @SerializedName("statistics") val statistics: YouTubeStatistics
)

data class YouTubeStatistics(
    @SerializedName("viewCount") val viewCount: String?,
    @SerializedName("likeCount") val likeCount: String?,
    @SerializedName("commentCount") val commentCount: String?
)

data class YouTubeChannelResponse(
    @SerializedName("items") val items: List<YouTubeChannelItem>
)

data class YouTubeChannelItem(
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: YouTubeChannelSnippet
)

data class YouTubeChannelSnippet(
    @SerializedName("title") val title: String,
    @SerializedName("thumbnails") val thumbnails: YouTubeThumbnails
)
