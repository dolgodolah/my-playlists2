package com.garfield.myplaylists.usecase.port.`in`

import com.garfield.myplaylists.domain.song.Song
import com.garfield.myplaylists.domain.song.SongSlice
import com.garfield.myplaylists.domain.song.YoutubeVideo
import com.garfield.myplaylists.usecase.ForbiddenException
import com.garfield.myplaylists.usecase.NotFoundException
import com.garfield.myplaylists.usecase.port.`in`.command.AddSongCommand
import com.garfield.myplaylists.usecase.port.`in`.command.DeleteSongCommand
import com.garfield.myplaylists.usecase.port.`in`.command.UpdateSongCommand
import com.garfield.myplaylists.usecase.port.`in`.query.GetPlaylistSongsQuery
import com.garfield.myplaylists.usecase.port.`in`.query.SearchYoutubeVideosQuery
import com.garfield.myplaylists.usecase.port.out.PlaylistRepository
import com.garfield.myplaylists.usecase.port.out.SongRepository
import com.garfield.myplaylists.usecase.port.out.YoutubeApi
import org.springframework.stereotype.Service

@Service
class SongUseCase(
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    private val youtubeApi: YoutubeApi,
) {
    fun getPlaylistSongs(query: GetPlaylistSongsQuery): SongSlice {
        val pageLimit = resolveLimit(query.limit)
        val playlist = playlistRepository.findByPlaylistId(query.playlistId)
            ?: throw NotFoundException("존재하지 않는 플레이리스트입니다.")

        if (!playlist.canBeViewedBy(query.memberCode)) {
            throw ForbiddenException("비공개 플레이리스트는 조회할 수 없습니다.")
        }

        val items = songRepository.findByPlaylistId(
            playlistId = query.playlistId,
            lastSongId = query.lastSongId,
            limit = pageLimit + 1,
        )
        val hasNext = items.size > pageLimit
        val songs = if (hasNext) items.take(pageLimit) else items
        return SongSlice(
            songs = songs,
            nextSongId = if (hasNext) songs.last().songId else null,
        )
    }

    fun addSong(command: AddSongCommand): Song {
        val playlist = playlistRepository.findByPlaylistId(command.playlistId)
            ?: throw NotFoundException("존재하지 않는 플레이리스트입니다.")

        if (!playlist.isOwnedBy(command.memberCode)) {
            throw ForbiddenException("노래 추가 권한이 없습니다.")
        }

        val created = songRepository.addSong(
            playlistId = command.playlistId,
            memberCode = command.memberCode,
            title = command.title,
            videoId = command.videoId,
            description = command.description,
        )
        playlistRepository.increaseSongCount(command.playlistId)
        return created
    }

    fun updateSong(command: UpdateSongCommand): Song {
        val playlist = playlistRepository.findByPlaylistId(command.playlistId)
            ?: throw NotFoundException("존재하지 않는 플레이리스트입니다.")
        if (!playlist.isOwnedBy(command.memberCode)) {
            throw ForbiddenException("노래 수정 권한이 없습니다.")
        }

        val song = songRepository.findBySongId(command.songId)
            ?: throw NotFoundException("존재하지 않는 노래입니다.")
        if (song.playlistId != command.playlistId) {
            throw NotFoundException("해당 플레이리스트의 노래가 아닙니다.")
        }

        return songRepository.updateSong(
            songId = command.songId,
            title = command.title,
            description = command.description,
        ) ?: throw NotFoundException("존재하지 않는 노래입니다.")
    }

    fun deleteSong(command: DeleteSongCommand) {
        val playlist = playlistRepository.findByPlaylistId(command.playlistId)
            ?: throw NotFoundException("존재하지 않는 플레이리스트입니다.")
        if (!playlist.isOwnedBy(command.memberCode)) {
            throw ForbiddenException("노래 삭제 권한이 없습니다.")
        }

        val song = songRepository.findBySongId(command.songId)
            ?: throw NotFoundException("존재하지 않는 노래입니다.")
        if (song.playlistId != command.playlistId) {
            throw NotFoundException("해당 플레이리스트의 노래가 아닙니다.")
        }

        if (!songRepository.deleteSong(command.songId)) {
            throw NotFoundException("존재하지 않는 노래입니다.")
        }
        playlistRepository.decreaseSongCount(command.playlistId)
    }

    fun searchYoutubeVideos(query: SearchYoutubeVideosQuery): List<YoutubeVideo> {
        val playlist = playlistRepository.findByPlaylistId(query.playlistId)
            ?: throw NotFoundException("존재하지 않는 플레이리스트입니다.")

        if (!playlist.isOwnedBy(query.memberCode)) {
            throw ForbiddenException("노래 추가 권한이 없습니다.")
        }

        val keyword = query.keyword.trim()
        if (keyword.isEmpty()) {
            throw IllegalArgumentException("검색어를 입력해주세요.")
        }
        if (keyword.length > maxSearchKeywordLength) {
            throw IllegalArgumentException("검색어는 최대 $maxSearchKeywordLength 자까지 입력할 수 있습니다.")
        }

        return youtubeApi.searchVideos(
            keyword = keyword,
            limit = youtubeSearchLimit,
        ).take(youtubeSearchLimit)
    }

    private fun resolveLimit(rawLimit: Int): Int {
        if (rawLimit < 1 || rawLimit > maxPageSize) {
            throw IllegalArgumentException("limit는 1 이상 $maxPageSize 이하여야 합니다.")
        }
        return rawLimit
    }

    companion object {
        private const val maxPageSize = 50
        private const val youtubeSearchLimit = 7
        private const val maxSearchKeywordLength = 120
    }
}
