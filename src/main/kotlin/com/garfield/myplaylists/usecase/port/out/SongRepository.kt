package com.garfield.myplaylists.usecase.port.out

import com.garfield.myplaylists.domain.song.Song

interface SongRepository {
    fun addSong(playlistId: Long, memberCode: String, title: String, videoId: String, description: String): Song
    fun findByPlaylistId(playlistId: Long, lastSongId: Long?, limit: Int): List<Song>
    fun findBySongId(songId: Long): Song?
    fun updateSong(songId: Long, title: String, description: String): Song?
    fun deleteSong(songId: Long): Boolean
}
