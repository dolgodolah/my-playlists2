package com.garfield.myplaylists.domain.playlist

data class PlaylistSlice(
    val playlists: List<Playlist>,
    val nextPlaylistId: Long?,
)
