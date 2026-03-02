package com.garfield.myplaylists

import com.garfield.myplaylists.adapter.`in`.web.AuthController
import com.garfield.myplaylists.adapter.`in`.web.PlaylistController
import com.garfield.myplaylists.adapter.`in`.web.SessionKeys
import com.garfield.myplaylists.adapter.`in`.web.model.request.LoginRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.CreatePlaylistRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.SignupRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.UpdatePlaylistRequest
import com.garfield.myplaylists.usecase.UnauthorizedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession

@SpringBootTest
class PlaylistControllerTest {

    @Autowired
    lateinit var authController: AuthController

    @Autowired
    lateinit var playlistController: PlaylistController

    @Test
    fun `my playlists returns starter playlist after signup and login`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "playlist_user"))
        val session = MockHttpSession()

        authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        val response = playlistController.getMyPlaylists(getSessionMemberCode(session))

        assertFalse(response.playlists.isEmpty())
        assertTrue(response.playlists.any { it.title.contains("첫 플레이리스트") })
    }

    @Test
    fun `all playlists returns public playlists`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "all_playlist_user"))
        val session = MockHttpSession()

        authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        val response = playlistController.getAllPlaylists(getSessionMemberCode(session))

        assertFalse(response.playlists.isEmpty())
        assertTrue(response.playlists.none { it.author == "사용자" })
    }

    @Test
    fun `all playlists requires authentication`() {
        assertThrows<UnauthorizedException> {
            playlistController.getAllPlaylists("")
        }
    }

    @Test
    fun `create playlist and find in my playlists`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "playlist_creator"))
        val session = MockHttpSession()

        authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        val createdResponse = playlistController.createPlaylist(
            request = CreatePlaylistRequest(
                title = "새 플레이리스트",
                description = "추가 기능 이관 테스트",
                visibility = true,
            ),
            memberCode = getSessionMemberCode(session),
        )

        assertEquals(200, createdResponse.statusCode.value())

        val myPlaylists = playlistController.getMyPlaylists(getSessionMemberCode(session)).playlists
        assertTrue(myPlaylists.any { it.title == "새 플레이리스트" && it.description == "추가 기능 이관 테스트" })
    }

    @Test
    fun `create playlist allows empty description`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "playlist_optional_description"))
        val session = MockHttpSession()

        authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        val createdResponse = playlistController.createPlaylist(
            request = CreatePlaylistRequest(
                title = "소개 없는 플레이리스트",
                description = "",
                visibility = true,
            ),
            memberCode = getSessionMemberCode(session),
        )
        assertEquals(200, createdResponse.statusCode.value())

        val myPlaylists = playlistController.getMyPlaylists(getSessionMemberCode(session)).playlists
        assertTrue(myPlaylists.any { it.title == "소개 없는 플레이리스트" && it.description.isEmpty() })
    }

    @Test
    fun `update playlist in my account`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "update_owner"))
        val session = MockHttpSession()

        authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        playlistController.createPlaylist(
            request = CreatePlaylistRequest(
                title = "수정 전 타이틀",
                description = "수정 전 설명",
                visibility = true,
            ),
            memberCode = getSessionMemberCode(session),
        )

        val target = playlistController.getMyPlaylists(getSessionMemberCode(session)).playlists
            .first { it.title == "수정 전 타이틀" }

        val response = playlistController.updatePlaylist(
            playlistId = target.playlistId.toLong(),
            request = UpdatePlaylistRequest(
                title = "수정 후 타이틀",
                description = "수정 후 설명",
                visibility = false,
            ),
            memberCode = getSessionMemberCode(session),
        )

        assertEquals(200, response.statusCode.value())
        val updated = playlistController.getMyPlaylists(getSessionMemberCode(session)).playlists
            .first { it.playlistId == target.playlistId }
        assertEquals("수정 후 타이틀", updated.title)
        assertEquals("수정 후 설명", updated.description)
        assertFalse(updated.visibility)
    }

    @Test
    fun `delete playlist in my account`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "delete_owner"))
        val session = MockHttpSession()

        authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        playlistController.createPlaylist(
            request = CreatePlaylistRequest(
                title = "삭제 대상 타이틀",
                description = "삭제 대상 설명",
                visibility = true,
            ),
            memberCode = getSessionMemberCode(session),
        )

        val target = playlistController.getMyPlaylists(getSessionMemberCode(session)).playlists
            .first { it.title == "삭제 대상 타이틀" }

        val response = playlistController.deletePlaylist(
            playlistId = target.playlistId.toLong(),
            memberCode = getSessionMemberCode(session),
        )
        assertEquals(200, response.statusCode.value())

        val myPlaylists = playlistController.getMyPlaylists(getSessionMemberCode(session)).playlists
        assertTrue(myPlaylists.none { it.playlistId == target.playlistId })
    }

    @Test
    fun `my playlists supports no offset pagination`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "pagination_owner"))
        val session = MockHttpSession()

        authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        repeat(12) { index ->
            playlistController.createPlaylist(
                request = CreatePlaylistRequest(
                    title = "페이징 테스트 $index",
                    description = "no offset pagination",
                    visibility = true,
                ),
                memberCode = getSessionMemberCode(session),
            )
        }

        val firstPage = playlistController.getMyPlaylists(
            memberCode = getSessionMemberCode(session),
            lastPlaylistId = null,
            limit = 10,
        )
        assertEquals(10, firstPage.playlists.size)
        assertTrue(firstPage.nextPlaylistId != null)

        val secondPage = playlistController.getMyPlaylists(
            memberCode = getSessionMemberCode(session),
            lastPlaylistId = firstPage.nextPlaylistId!!.toLong(),
            limit = 10,
        )
        assertTrue(secondPage.playlists.isNotEmpty())
    }

    private fun getSessionMemberCode(session: MockHttpSession): String {
        return session.getAttribute(SessionKeys.MEMBER_CODE) as String
    }
}
