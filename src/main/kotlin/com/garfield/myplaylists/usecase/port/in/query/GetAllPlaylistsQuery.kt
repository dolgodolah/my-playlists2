package com.garfield.myplaylists.usecase.port.`in`.query

data class GetAllPlaylistsQuery(
    val lastPlaylistId: Long? = null,
    val limit: Int = 10,
)
