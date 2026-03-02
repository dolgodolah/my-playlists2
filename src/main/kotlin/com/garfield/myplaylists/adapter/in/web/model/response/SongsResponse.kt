package com.garfield.myplaylists.adapter.`in`.web.model.response

data class SongsResponse(
    val songs: List<SongResponse>,
    val nextSongId: String?,
)
