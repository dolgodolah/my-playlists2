package com.garfield.myplaylists.adapter.`in`.web

import com.garfield.myplaylists.adapter.`in`.web.model.request.AddSongRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.UpdateSongRequest
import com.garfield.myplaylists.adapter.`in`.web.model.response.SongResponse
import com.garfield.myplaylists.adapter.`in`.web.model.response.SongsResponse
import com.garfield.myplaylists.adapter.`in`.web.model.response.YoutubeVideosResponse
import com.garfield.myplaylists.adapter.`in`.web.model.response.YoutubeVideoResponse
import com.garfield.myplaylists.domain.song.Song
import com.garfield.myplaylists.domain.song.YoutubeVideo
import com.garfield.myplaylists.usecase.port.`in`.SongUseCase
import com.garfield.myplaylists.usecase.port.`in`.command.AddSongCommand
import com.garfield.myplaylists.usecase.port.`in`.command.DeleteSongCommand
import com.garfield.myplaylists.usecase.port.`in`.command.UpdateSongCommand
import com.garfield.myplaylists.usecase.port.`in`.query.GetPlaylistSongsQuery
import com.garfield.myplaylists.usecase.port.`in`.query.SearchYoutubeVideosQuery
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.DeleteMapping
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/v1/playlists/{playlistId}/songs")
class SongController(
    private val songUseCase: SongUseCase,
) {
    @GetMapping
    fun getPlaylistSongs(
        @PathVariable playlistId: Long,
        @SessionMemberCode memberCode: String,
        @RequestParam(required = false) lastSongId: Long? = null,
        @RequestParam(defaultValue = defaultPageSizeText) limit: Int = defaultPageSize,
    ): SongsResponse {
        val songSlice = songUseCase.getPlaylistSongs(
            GetPlaylistSongsQuery(
                memberCode = memberCode,
                playlistId = playlistId,
                lastSongId = lastSongId,
                limit = limit,
            ),
        )
        return SongsResponse(
            songs = songSlice.songs.map { it.toResponse() },
            nextSongId = songSlice.nextSongId?.toString(),
        )
    }

    @PostMapping
    fun addSong(
        @PathVariable playlistId: Long,
        @Valid @RequestBody request: AddSongRequest,
        @SessionMemberCode memberCode: String,
    ): ResponseEntity<Void> {
        songUseCase.addSong(
            AddSongCommand(
                memberCode = memberCode,
                playlistId = playlistId,
                title = request.title,
                videoId = request.videoId,
                description = request.description,
            ),
        )
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{songId}")
    fun updateSong(
        @PathVariable playlistId: Long,
        @PathVariable songId: Long,
        @Valid @RequestBody request: UpdateSongRequest,
        @SessionMemberCode memberCode: String,
    ): ResponseEntity<Void> {
        songUseCase.updateSong(
            UpdateSongCommand(
                memberCode = memberCode,
                playlistId = playlistId,
                songId = songId,
                title = request.title,
                description = request.description,
            ),
        )
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{songId}")
    fun deleteSong(
        @PathVariable playlistId: Long,
        @PathVariable songId: Long,
        @SessionMemberCode memberCode: String,
    ): ResponseEntity<Void> {
        songUseCase.deleteSong(
            DeleteSongCommand(
                memberCode = memberCode,
                playlistId = playlistId,
                songId = songId,
            ),
        )
        return ResponseEntity.ok().build()
    }

    @GetMapping("/search")
    fun searchYoutubeSongs(
        @PathVariable playlistId: Long,
        @SessionMemberCode memberCode: String,
        @RequestParam q: String,
    ): YoutubeVideosResponse {
        val youtubeVideos = songUseCase.searchYoutubeVideos(
            SearchYoutubeVideosQuery(
                memberCode = memberCode,
                playlistId = playlistId,
                keyword = q,
            ),
        )
        return YoutubeVideosResponse(
            songs = youtubeVideos.map { it.toYoutubeSearchResponse() },
        )
    }

    private fun Song.toResponse(): SongResponse = SongResponse(
        songId = songId.toString(),
        title = title,
        videoId = videoId,
        description = description,
        createdDate = createdAt.format(dateTimeFormatter),
        updatedDate = updatedAt.format(dateTimeFormatter),
    )

    private fun YoutubeVideo.toYoutubeSearchResponse(): YoutubeVideoResponse = YoutubeVideoResponse(
        title = title,
        videoId = videoId,
    )

    companion object {
        private const val defaultPageSize = 10
        private const val defaultPageSizeText = "10"
        private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
