package com.garfield.myplaylists.adapter.out.persistence.entity

import java.time.LocalDateTime

data class PlaylistEntity(
    val playlistId: Long,
    val memberCode: String,
    val title: String,
    val description: String,
    val updatedAt: LocalDateTime,
    val visibility: Boolean,
    val songCount: Int,
)
