package com.garfield.myplaylists.domain.playlist

import java.time.LocalDateTime

data class Playlist(
    val playlistId: Long,
    val memberCode: String,
    val title: String,
    val description: String,
    val updatedAt: LocalDateTime,
    val visibility: Boolean,
    val songCount: Int,
) {
    fun isOwnedBy(memberCode: String): Boolean = this.memberCode == memberCode

    fun canBeViewedBy(memberCode: String): Boolean = visibility || isOwnedBy(memberCode)
}
