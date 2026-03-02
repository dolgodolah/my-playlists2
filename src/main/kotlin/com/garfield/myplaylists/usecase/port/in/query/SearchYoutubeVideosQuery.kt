package com.garfield.myplaylists.usecase.port.`in`.query

data class SearchYoutubeVideosQuery(
    val memberCode: String,
    val playlistId: Long,
    val keyword: String,
)
