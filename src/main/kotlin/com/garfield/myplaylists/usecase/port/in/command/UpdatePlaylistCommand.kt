package com.garfield.myplaylists.usecase.port.`in`.command

data class UpdatePlaylistCommand(
    val memberCode: String,
    val playlistId: Long,
    val title: String,
    val description: String,
    val visibility: Boolean,
)
