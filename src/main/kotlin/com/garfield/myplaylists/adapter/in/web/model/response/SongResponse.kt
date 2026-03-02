package com.garfield.myplaylists.adapter.`in`.web.model.response

data class SongResponse(
    val songId: String,
    val title: String,
    val videoId: String,
    val description: String,
    val createdDate: String,
    val updatedDate: String,
)