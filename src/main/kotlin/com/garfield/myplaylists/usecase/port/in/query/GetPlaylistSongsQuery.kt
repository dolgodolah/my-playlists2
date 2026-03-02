package com.garfield.myplaylists.usecase.port.`in`.query

data class GetPlaylistSongsQuery(
    val memberCode: String,
    val playlistId: Long,
    val lastSongId: Long? = null,
    val limit: Int = 10,
)
