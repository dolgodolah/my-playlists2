package com.garfield.myplaylists.usecase.port.`in`.command

data class DeletePlaylistCommand(
    val memberCode: String,
    val playlistId: Long,
)
