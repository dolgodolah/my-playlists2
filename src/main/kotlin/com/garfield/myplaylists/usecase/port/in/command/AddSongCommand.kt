package com.garfield.myplaylists.usecase.port.`in`.command

data class AddSongCommand(
    val memberCode: String,
    val playlistId: Long,
    val title: String,
    val videoId: String,
    val description: String,
)
