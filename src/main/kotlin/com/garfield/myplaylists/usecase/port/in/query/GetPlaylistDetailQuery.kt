package com.garfield.myplaylists.usecase.port.`in`.query

data class GetPlaylistDetailQuery(
    val memberCode: String,
    val playlistId: Long,
)
