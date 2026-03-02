package com.garfield.myplaylists.domain.song

import java.time.LocalDateTime

data class Song(
    val songId: Long,
    val playlistId: Long,
    val memberCode: String,
    val title: String,
    val videoId: String,
    val description: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
