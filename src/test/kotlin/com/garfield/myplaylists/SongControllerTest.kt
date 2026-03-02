package com.garfield.myplaylists

import com.garfield.myplaylists.adapter.`in`.web.AuthController
import com.garfield.myplaylists.adapter.`in`.web.PlaylistController
import com.garfield.myplaylists.adapter.`in`.web.SongController
import com.garfield.myplaylists.adapter.`in`.web.SessionKeys
import com.garfield.myplaylists.adapter.`in`.web.model.request.AddSongRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.CreatePlaylistRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.LoginRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.SignupRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.UpdateSongRequest
import com.garfield.myplaylists.usecase.ForbiddenException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession

@SpringBootTest
class SongControllerTest {

    @Autowired
    lateinit var authController: AuthController

    @Autowired
    lateinit var playlistController: PlaylistController

    @Autowired
    lateinit var songController: SongController

    @Test
    fun `add song in playlist detail flow`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "song_owner"))
        val session = MockHttpSession()

        authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        playlistController.createPlaylist(
            request = CreatePlaylistRequest(
                title = "노래 테스트 플레이리스트",
                description = "노래 추가 테스트",
                visibility = true,
            ),
            memberCode = getSessionMemberCode(session),
        )

        val playlistId = playlistController.getMyPlaylists(getSessionMemberCode(session))
            .playlists
            .first { it.title == "노래 테스트 플레이리스트" }
            .playlistId
            .toLong()

        val addResponse = songController.addSong(
            playlistId = playlistId,
            request = AddSongRequest(
                title = "테스트 곡",
                videoId = "dQw4w9WgXcQ",
                description = "샘플 설명",
            ),
            memberCode = getSessionMemberCode(session),
        )
        assertEquals(200, addResponse.statusCode.value())

        val playlist = playlistController.getPlaylistDetail(
            playlistId = playlistId,
            memberCode = getSessionMemberCode(session),
        )
        val songs = songController.getPlaylistSongs(
            playlistId = playlistId,
            memberCode = getSessionMemberCode(session),
        )
        assertEquals("노래 테스트 플레이리스트", playlist.title)
        assertTrue(songs.songs.any { it.title == "테스트 곡" && it.videoId == "dQw4w9WgXcQ" })
    }

    @Test
    fun `playlist detail songs supports no offset pagination`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "song_page_owner"))
        val session = MockHttpSession()

        authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        playlistController.createPlaylist(
            request = CreatePlaylistRequest(
                title = "노래 페이징 테스트",
                description = "곡 목록 페이징",
                visibility = true,
            ),
            memberCode = getSessionMemberCode(session),
        )

        val playlistId = playlistController.getMyPlaylists(getSessionMemberCode(session))
            .playlists
            .first { it.title == "노래 페이징 테스트" }
            .playlistId
            .toLong()

        repeat(12) { index ->
            songController.addSong(
                playlistId = playlistId,
                request = AddSongRequest(
                    title = "테스트 곡 $index",
                    videoId = "video-$index",
                    description = "desc-$index",
                ),
                memberCode = getSessionMemberCode(session),
            )
        }

        val firstPage = songController.getPlaylistSongs(
            playlistId = playlistId,
            memberCode = getSessionMemberCode(session),
            lastSongId = null,
            limit = 10,
        )
        assertEquals(10, firstPage.songs.size)
        assertTrue(firstPage.nextSongId != null)

        val secondPage = songController.getPlaylistSongs(
            playlistId = playlistId,
            memberCode = getSessionMemberCode(session),
            lastSongId = firstPage.nextSongId!!.toLong(),
            limit = 10,
        )
        assertTrue(secondPage.songs.isNotEmpty())
    }

    @Test
    fun `youtube search for song add returns result list response`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "youtube_owner"))
        val session = MockHttpSession()
        authController.login(LoginRequest(memberCode = signupResponse.issuedCode), session)

        playlistController.createPlaylist(
            request = CreatePlaylistRequest(
                title = "유튜브 검색 테스트",
                description = "검색 결과 확인",
                visibility = true,
            ),
            memberCode = getSessionMemberCode(session),
        )

        val playlistId = playlistController.getMyPlaylists(getSessionMemberCode(session))
            .playlists
            .first { it.title == "유튜브 검색 테스트" }
            .playlistId
            .toLong()

        val response = songController.searchYoutubeSongs(
            playlistId = playlistId,
            memberCode = getSessionMemberCode(session),
            q = "아이유 밤편지",
        )
        assertTrue(response.songs.size <= 7)
    }

    @Test
    fun `youtube search requires owner permission`() {
        val ownerSignup = authController.signup(SignupRequest(nickname = "youtube_permission_owner"))
        val ownerSession = MockHttpSession()
        authController.login(LoginRequest(memberCode = ownerSignup.issuedCode), ownerSession)

        playlistController.createPlaylist(
            request = CreatePlaylistRequest(
                title = "권한 테스트 플레이리스트",
                description = "owner만 검색 가능",
                visibility = true,
            ),
            memberCode = getSessionMemberCode(ownerSession),
        )

        val playlistId = playlistController.getMyPlaylists(getSessionMemberCode(ownerSession))
            .playlists
            .first { it.title == "권한 테스트 플레이리스트" }
            .playlistId
            .toLong()

        val otherSignup = authController.signup(SignupRequest(nickname = "youtube_permission_other"))
        val otherSession = MockHttpSession()
        authController.login(LoginRequest(memberCode = otherSignup.issuedCode), otherSession)

        assertThrows<ForbiddenException> {
            songController.searchYoutubeSongs(
                playlistId = playlistId,
                memberCode = getSessionMemberCode(otherSession),
                q = "검색어",
            )
        }
    }

    @Test
    fun `owner can update song title and description`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "song_update_owner"))
        val session = MockHttpSession()
        authController.login(LoginRequest(memberCode = signupResponse.issuedCode), session)

        playlistController.createPlaylist(
            request = CreatePlaylistRequest(
                title = "수정 테스트 플레이리스트",
                description = "수정 테스트",
                visibility = true,
            ),
            memberCode = getSessionMemberCode(session),
        )
        val playlistId = playlistController.getMyPlaylists(getSessionMemberCode(session))
            .playlists
            .first { it.title == "수정 테스트 플레이리스트" }
            .playlistId
            .toLong()

        songController.addSong(
            playlistId = playlistId,
            request = AddSongRequest(
                title = "수정 전 제목",
                videoId = "update-video-id",
                description = "수정 전 설명",
            ),
            memberCode = getSessionMemberCode(session),
        )
        val targetSong = songController.getPlaylistSongs(
            playlistId = playlistId,
            memberCode = getSessionMemberCode(session),
        ).songs.first { it.title == "수정 전 제목" }

        val response = songController.updateSong(
            playlistId = playlistId,
            songId = targetSong.songId.toLong(),
            request = UpdateSongRequest(
                title = "수정 후 제목",
                description = "수정 후 설명",
            ),
            memberCode = getSessionMemberCode(session),
        )
        assertEquals(200, response.statusCode.value())

        val updatedSongs = songController.getPlaylistSongs(
            playlistId = playlistId,
            memberCode = getSessionMemberCode(session),
        ).songs
        assertTrue(updatedSongs.any { it.title == "수정 후 제목" && it.description == "수정 후 설명" })
    }

    @Test
    fun `owner can delete song and song count decreases`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "song_delete_owner"))
        val session = MockHttpSession()
        authController.login(LoginRequest(memberCode = signupResponse.issuedCode), session)

        playlistController.createPlaylist(
            request = CreatePlaylistRequest(
                title = "삭제 테스트 플레이리스트",
                description = "삭제 테스트",
                visibility = true,
            ),
            memberCode = getSessionMemberCode(session),
        )
        val playlistId = playlistController.getMyPlaylists(getSessionMemberCode(session))
            .playlists
            .first { it.title == "삭제 테스트 플레이리스트" }
            .playlistId
            .toLong()

        songController.addSong(
            playlistId = playlistId,
            request = AddSongRequest(
                title = "삭제할 곡",
                videoId = "delete-video-id",
                description = "삭제 대상",
            ),
            memberCode = getSessionMemberCode(session),
        )

        val songsBeforeDelete = songController.getPlaylistSongs(
            playlistId = playlistId,
            memberCode = getSessionMemberCode(session),
        ).songs
        val targetSongId = songsBeforeDelete.first { it.title == "삭제할 곡" }.songId.toLong()

        val deleteResponse = songController.deleteSong(
            playlistId = playlistId,
            songId = targetSongId,
            memberCode = getSessionMemberCode(session),
        )
        assertEquals(200, deleteResponse.statusCode.value())

        val songsAfterDelete = songController.getPlaylistSongs(
            playlistId = playlistId,
            memberCode = getSessionMemberCode(session),
        ).songs
        assertFalse(songsAfterDelete.any { it.songId.toLong() == targetSongId })

        val playlistAfterDelete = playlistController.getPlaylistDetail(
            playlistId = playlistId,
            memberCode = getSessionMemberCode(session),
        )
        assertEquals(songsAfterDelete.size, playlistAfterDelete.songCount)
    }

    private fun getSessionMemberCode(session: MockHttpSession): String {
        return session.getAttribute(SessionKeys.MEMBER_CODE) as String
    }
}
