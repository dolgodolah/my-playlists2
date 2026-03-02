package com.garfield.myplaylists.adapter.`in`.web.model.response

data class YoutubeVideosResponse(
    val songs: List<YoutubeVideoResponse>,
)

data class YoutubeVideoResponse(
    val title: String,
    val videoId: String,
)
