package com.garfield.myplaylists.usecase.port.`in`.query

data class GetMyPlaylistsQuery(
    val memberCode: String,
    val lastPlaylistId: Long? = null,
    val limit: Int = 10,
)
