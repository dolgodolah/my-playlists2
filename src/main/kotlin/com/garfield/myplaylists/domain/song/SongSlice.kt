package com.garfield.myplaylists.domain.song

data class SongSlice(
    val songs: List<Song>,
    val nextSongId: Long?,
)
