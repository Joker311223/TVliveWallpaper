package com.example.tv.model

data class VideoSource(
    val id: String,
    val url: String,
    val title: String,
    val isLocal: Boolean = false
)

data class VideoListResponse(
    val videos: List<VideoInfo>
)

data class VideoInfo(
    val id: String,
    val url: String,
    val title: String
)
