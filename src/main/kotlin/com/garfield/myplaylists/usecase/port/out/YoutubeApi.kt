package com.garfield.myplaylists.usecase.port.out

import com.garfield.myplaylists.domain.song.YoutubeVideo

interface YoutubeApi {
    fun searchVideos(keyword: String, limit: Int): List<YoutubeVideo>
}
