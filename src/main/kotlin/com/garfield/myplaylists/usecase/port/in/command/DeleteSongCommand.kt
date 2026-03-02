package com.garfield.myplaylists.usecase.port.`in`.command

data class DeleteSongCommand(
    val memberCode: String,
    val playlistId: Long,
    val songId: Long,
)
