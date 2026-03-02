package com.garfield.myplaylists.adapter.out.persistence.entity

import java.time.LocalDateTime

data class SongEntity(
    val songId: Long,
    val playlistId: Long,
    val memberCode: String,
    val title: String,
    val videoId: String,
    val description: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
