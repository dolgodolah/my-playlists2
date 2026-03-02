package com.garfield.myplaylists.adapter.`in`.web

import com.garfield.myplaylists.adapter.`in`.web.model.request.CreatePlaylistRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.UpdatePlaylistRequest
import com.garfield.myplaylists.adapter.`in`.web.model.response.PlaylistResponse
import com.garfield.myplaylists.adapter.`in`.web.model.response.PlaylistsResponse
import com.garfield.myplaylists.domain.playlist.Playlist
import com.garfield.myplaylists.usecase.port.`in`.MemberUseCase
import com.garfield.myplaylists.usecase.port.`in`.PlaylistUseCase
import com.garfield.myplaylists.usecase.port.`in`.command.CreatePlaylistCommand
import com.garfield.myplaylists.usecase.port.`in`.command.DeletePlaylistCommand
import com.garfield.myplaylists.usecase.port.`in`.command.UpdatePlaylistCommand
import com.garfield.myplaylists.usecase.port.`in`.query.GetAllPlaylistsQuery
import com.garfield.myplaylists.usecase.port.`in`.query.GetPlaylistDetailQuery
import com.garfield.myplaylists.usecase.port.`in`.query.GetMyPlaylistsQuery
import com.garfield.myplaylists.usecase.port.`in`.query.GetMemberQuery
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/v1/playlists")
class PlaylistController(
    private val memberUseCase: MemberUseCase,
    private val playlistUseCase: PlaylistUseCase,
) {
    @GetMapping
    fun getMyPlaylists(
        @SessionMemberCode memberCode: String,
        @RequestParam(required = false) lastPlaylistId: Long? = null,
        @RequestParam(defaultValue = defaultPageSizeText) limit: Int = defaultPageSize,
    ): PlaylistsResponse {
        val member = memberUseCase.execute(
            GetMemberQuery(memberCode = memberCode)
        )

        val slice = playlistUseCase.findMyPlaylists(
            GetMyPlaylistsQuery(
                memberCode = member.memberCode,
                lastPlaylistId = lastPlaylistId,
                limit = limit,
            )
        )

        return PlaylistsResponse(
            playlists = slice.playlists.map { playlist ->
                playlist.toResponse(
                    isEditable = true,
                    author = member.nickname
                )
            },
            nextPlaylistId = slice.nextPlaylistId?.toString(),
        )
    }

    @GetMapping("/all")
    fun getAllPlaylists(
        @SessionMemberCode memberCode: String,
        @RequestParam(required = false) lastPlaylistId: Long? = null,
        @RequestParam(defaultValue = defaultPageSizeText) limit: Int = defaultPageSize,
    ): PlaylistsResponse {
        memberUseCase.execute(
            GetMemberQuery(memberCode = memberCode)
        )

        val slice = playlistUseCase.findAllPlaylists(
            GetAllPlaylistsQuery(
                lastPlaylistId = lastPlaylistId,
                limit = limit,
            )
        )

        return PlaylistsResponse(
            playlists = slice.playlists.map { playlist ->
                val member = memberUseCase.execute(GetMemberQuery(playlist.memberCode))
                playlist.toResponse(
                    isEditable = false,
                    author = member.nickname
                )
            },
            nextPlaylistId = slice.nextPlaylistId?.toString(),
        )
    }

    @GetMapping("/{playlistId}")
    fun getPlaylistDetail(
        @PathVariable playlistId: Long,
        @SessionMemberCode memberCode: String,
    ): PlaylistResponse {
        val playlist = playlistUseCase.getPlaylistDetail(
            GetPlaylistDetailQuery(
                memberCode = memberCode,
                playlistId = playlistId,
            ),
        )
        val owner = memberUseCase.execute(GetMemberQuery(playlist.memberCode))
        val isEditable = playlist.isOwnedBy(memberCode)
        return playlist.toResponse(author = owner.nickname, isEditable = isEditable)
    }

    @PostMapping
    fun createPlaylist(
        @Valid @RequestBody request: CreatePlaylistRequest,
        @SessionMemberCode memberCode: String,
    ): ResponseEntity<Void> {
        playlistUseCase.createPlaylist(
            CreatePlaylistCommand(
                memberCode = memberCode,
                title = request.title,
                description = request.description,
                visibility = request.visibility,
            )
        )
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{playlistId}")
    fun updatePlaylist(
        @PathVariable playlistId: Long,
        @Valid @RequestBody request: UpdatePlaylistRequest,
        @SessionMemberCode memberCode: String,
    ): ResponseEntity<Void> {
        playlistUseCase.updatePlaylist(
            UpdatePlaylistCommand(
                memberCode = memberCode,
                playlistId = playlistId,
                title = request.title,
                description = request.description,
                visibility = request.visibility,
            )
        )
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{playlistId}")
    fun deletePlaylist(
        @PathVariable playlistId: Long,
        @SessionMemberCode memberCode: String,
    ): ResponseEntity<Void> {
        playlistUseCase.deletePlaylist(
            DeletePlaylistCommand(
                memberCode = memberCode,
                playlistId = playlistId,
            )
        )
        return ResponseEntity.ok().build()
    }

    private fun Playlist.toResponse(isEditable: Boolean, author: String): PlaylistResponse {
        return PlaylistResponse(
            playlistId = this.playlistId.toString(),
            title = this.title,
            description = this.description,
            updatedDate = this.updatedAt.format(dateTimeFormatter),
            visibility = this.visibility,
            author = author,
            songCount = this.songCount,
            isEditable = isEditable,
        )
    }

    companion object {
        private const val defaultPageSize = 10
        private const val defaultPageSizeText = "10"
        private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
