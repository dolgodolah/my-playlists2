package com.garfield.myplaylists.adapter.`in`.web.model.response

data class PlaylistResponse(
    val playlistId: String,
    val title: String,
    val description: String,
    val updatedDate: String,
    val visibility: Boolean,
    val author: String,
    val songCount: Int,
    val isEditable: Boolean,
)

data class PlaylistsResponse(
    val playlists: List<PlaylistResponse>,
    val nextPlaylistId: String?,
)
