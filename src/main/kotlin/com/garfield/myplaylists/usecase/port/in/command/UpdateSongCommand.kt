package com.garfield.myplaylists.usecase.port.`in`.command

data class UpdateSongCommand(
    val memberCode: String,
    val playlistId: Long,
    val songId: Long,
    val title: String,
    val description: String,
)
