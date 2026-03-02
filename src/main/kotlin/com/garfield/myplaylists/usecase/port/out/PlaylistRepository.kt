package com.garfield.myplaylists.usecase.port.out

import com.garfield.myplaylists.domain.auth.Member
import com.garfield.myplaylists.domain.playlist.Playlist

interface PlaylistRepository {
    fun createStarterPlaylistForMember(member: Member)
    fun createPlaylist(member: Member, title: String, description: String, visibility: Boolean): Playlist
    fun findByPlaylistId(playlistId: Long): Playlist?
    fun increaseSongCount(playlistId: Long): Playlist?
    fun decreaseSongCount(playlistId: Long): Playlist?
    fun updatePlaylist(playlistId: Long, title: String, description: String, visibility: Boolean): Playlist?
    fun deletePlaylist(playlistId: Long): Boolean
    fun findByOwnerMemberCode(memberCode: String, lastPlaylistId: Long?, limit: Int): List<Playlist>
    fun findPublicPlaylists(lastPlaylistId: Long?, limit: Int): List<Playlist>
}
