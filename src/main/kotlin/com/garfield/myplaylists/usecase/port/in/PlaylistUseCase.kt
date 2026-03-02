package com.garfield.myplaylists.usecase.port.`in`

import com.garfield.myplaylists.domain.playlist.Playlist
import com.garfield.myplaylists.domain.playlist.PlaylistSlice
import com.garfield.myplaylists.usecase.ForbiddenException
import com.garfield.myplaylists.usecase.NotFoundException
import com.garfield.myplaylists.usecase.UnauthorizedException
import com.garfield.myplaylists.usecase.port.`in`.command.CreatePlaylistCommand
import com.garfield.myplaylists.usecase.port.`in`.command.DeletePlaylistCommand
import com.garfield.myplaylists.usecase.port.`in`.command.UpdatePlaylistCommand
import com.garfield.myplaylists.usecase.port.`in`.query.GetAllPlaylistsQuery
import com.garfield.myplaylists.usecase.port.`in`.query.GetMyPlaylistsQuery
import com.garfield.myplaylists.usecase.port.`in`.query.GetPlaylistDetailQuery
import com.garfield.myplaylists.usecase.port.out.MemberRepository
import com.garfield.myplaylists.usecase.port.out.PlaylistRepository
import org.springframework.stereotype.Service

@Service
class PlaylistUseCase(
    private val memberRepository: MemberRepository,
    private val playlistRepository: PlaylistRepository,
) {
    fun findMyPlaylists(query: GetMyPlaylistsQuery): PlaylistSlice {
        val pageLimit = resolveLimit(query.limit)
        val items = playlistRepository.findByOwnerMemberCode(
            memberCode = query.memberCode,
            lastPlaylistId = query.lastPlaylistId,
            limit = pageLimit + 1,
        )
        val hasNext = items.size > pageLimit
        val playlists = if (hasNext) items.take(pageLimit) else items
        return PlaylistSlice(
            playlists = playlists,
            nextPlaylistId = if (hasNext) playlists.last().playlistId else null,
        )
    }

    fun findAllPlaylists(query: GetAllPlaylistsQuery): PlaylistSlice {
        val pageLimit = resolveLimit(query.limit)
        val items = playlistRepository.findPublicPlaylists(
            lastPlaylistId = query.lastPlaylistId,
            limit = pageLimit + 1,
        )
        val hasNext = items.size > pageLimit
        val playlists = if (hasNext) items.take(pageLimit) else items
        return PlaylistSlice(
            playlists = playlists,
            nextPlaylistId = if (hasNext) playlists.last().playlistId else null,
        )
    }

    fun createPlaylist(command: CreatePlaylistCommand): Playlist {
        val member = memberRepository.findByMemberCode(command.memberCode)
            ?: throw UnauthorizedException("로그인이 필요합니다.")

        return playlistRepository.createPlaylist(
            member = member,
            title = command.title,
            description = command.description,
            visibility = command.visibility,
        )
    }

    fun getPlaylistDetail(query: GetPlaylistDetailQuery): Playlist {
        val playlist = playlistRepository.findByPlaylistId(query.playlistId)
            ?: throw NotFoundException("존재하지 않는 플레이리스트입니다.")

        if (!playlist.canBeViewedBy(query.memberCode)) {
            throw ForbiddenException("비공개 플레이리스트는 조회할 수 없습니다.")
        }
        return playlist
    }

    fun updatePlaylist(command: UpdatePlaylistCommand): Playlist {
        val playlist = playlistRepository.findByPlaylistId(command.playlistId)
            ?: throw NotFoundException("존재하지 않는 플레이리스트입니다.")

        if (!playlist.isOwnedBy(command.memberCode)) {
            throw ForbiddenException("플레이리스트 수정 권한이 없습니다.")
        }

        return playlistRepository.updatePlaylist(
            playlistId = command.playlistId,
            title = command.title,
            description = command.description,
            visibility = command.visibility,
        ) ?: throw NotFoundException("존재하지 않는 플레이리스트입니다.")
    }

    fun deletePlaylist(command: DeletePlaylistCommand) {
        val playlist = playlistRepository.findByPlaylistId(command.playlistId)
            ?: throw NotFoundException("존재하지 않는 플레이리스트입니다.")

        if (!playlist.isOwnedBy(command.memberCode)) {
            throw ForbiddenException("플레이리스트 삭제 권한이 없습니다.")
        }

        if (!playlistRepository.deletePlaylist(command.playlistId)) {
            throw NotFoundException("존재하지 않는 플레이리스트입니다.")
        }
    }

    private fun resolveLimit(rawLimit: Int): Int {
        if (rawLimit !in 1..maxPageSize) {
            throw IllegalArgumentException("limit는 1 이상 $maxPageSize 이하여야 합니다.")
        }
        return rawLimit
    }

    companion object {
        private const val maxPageSize = 50
    }
}
