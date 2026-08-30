package com.blankrk.app.data.model

data class ShortVideo(
    val videoId: String = "",
    val title: String = "",
    val channelId: String = "",
    val channelTitle: String = "",
    val channelAvatarUrl: String = "",
    val thumbnailUrl: String = "",
    var likeCount: Long = 0,
    var commentCount: Long = 0,
    var isLikedByUser: Boolean = false,
    var isFollowingChannel: Boolean = false
)
