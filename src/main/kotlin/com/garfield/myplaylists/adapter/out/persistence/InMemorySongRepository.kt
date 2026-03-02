package com.garfield.myplaylists.adapter.out.persistence

import com.garfield.myplaylists.adapter.out.persistence.entity.SongEntity
import com.garfield.myplaylists.adapter.out.persistence.mapper.toDomain
import com.garfield.myplaylists.domain.song.Song
import com.garfield.myplaylists.usecase.port.out.SongRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class InMemorySongRepository : SongRepository {
    private val sequence = AtomicLong(0)
    private val songsById = ConcurrentHashMap<Long, SongEntity>()

    override fun addSong(
        playlistId: Long,
        memberCode: String,
        title: String,
        videoId: String,
        description: String,
    ): Song {
        val id = sequence.incrementAndGet()
        val now = LocalDateTime.now()
        val entity = SongEntity(
            songId = id,
            playlistId = playlistId,
            memberCode = memberCode,
            title = title,
            videoId = videoId,
            description = description,
            createdAt = now,
            updatedAt = now,
        )
        songsById[id] = entity
        return entity.toDomain()
    }

    override fun findByPlaylistId(playlistId: Long, lastSongId: Long?, limit: Int): List<Song> {
        return songsById.values
            .filter { it.playlistId == playlistId }
            .filter { lastSongId == null || it.songId < lastSongId }
            .sortedByDescending { it.songId }
            .take(limit)
            .map { it.toDomain() }
    }

    override fun findBySongId(songId: Long): Song? = songsById[songId]?.toDomain()

    override fun updateSong(songId: Long, title: String, description: String): Song? {
        val current = songsById[songId] ?: return null
        val updated = current.copy(
            title = title,
            description = description,
            updatedAt = LocalDateTime.now(),
        )
        songsById[songId] = updated
        return updated.toDomain()
    }

    override fun deleteSong(songId: Long): Boolean = songsById.remove(songId) != null
}
