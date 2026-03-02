package com.garfield.myplaylists.usecase.port.`in`.command

data class CreatePlaylistCommand(
    val memberCode: String,
    val title: String,
    val description: String,
    val visibility: Boolean,
)
