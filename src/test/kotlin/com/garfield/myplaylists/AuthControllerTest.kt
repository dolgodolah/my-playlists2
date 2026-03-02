package com.garfield.myplaylists

import com.garfield.myplaylists.adapter.`in`.web.AuthController
import com.garfield.myplaylists.adapter.`in`.web.PlaylistController
import com.garfield.myplaylists.adapter.`in`.web.SessionKeys
import com.garfield.myplaylists.adapter.`in`.web.model.request.LoginRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.SignupRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.UpdateNicknameRequest
import com.garfield.myplaylists.usecase.UnauthorizedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession

@SpringBootTest
class AuthControllerTest {

    @Autowired
    lateinit var authController: AuthController

    @Autowired
    lateinit var playlistController: PlaylistController

    @Test
    fun `signup - login - session - logout flow`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "tester_01"))
        assertEquals("tester_01", signupResponse.nickname)
        assertEquals(12, signupResponse.issuedCode.length)

        val session = MockHttpSession()

        val loginResponse = authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        assertEquals("tester_01", loginResponse.nickname)

        val sessionResponse = authController.session(getSessionMemberCode(session))
        assertEquals("tester_01", sessionResponse.nickname)

        val logoutResponse = authController.logout(session)
        assertEquals(204, logoutResponse.statusCode.value())

        assertThrows(UnauthorizedException::class.java) {
            authController.session("")
        }
    }

    @Test
    fun `login fails when member code invalid`() {
        val session = MockHttpSession()

        val exception = assertThrows(UnauthorizedException::class.java) {
            authController.login(
                request = LoginRequest(memberCode = "INVALIDCODE"),
                session = session,
            )
        }

        assertNotNull(exception)
        assertEquals("유효하지 않은 회원코드입니다.", exception.message)
    }

    @Test
    fun `update my nickname`() {
        val signupResponse = authController.signup(SignupRequest(nickname = "before_name"))
        val session = MockHttpSession()

        authController.login(
            request = LoginRequest(memberCode = signupResponse.issuedCode),
            session = session,
        )

        val updated = authController.updateMyNickname(
            request = UpdateNicknameRequest(nickname = "after_name"),
            memberCode = getSessionMemberCode(session),
        )

        assertEquals("after_name", updated.nickname)

        val sessionResponse = authController.session(getSessionMemberCode(session))
        assertEquals("after_name", sessionResponse.nickname)

        val myPlaylistsResponse = playlistController.getMyPlaylists(getSessionMemberCode(session))
        assertEquals("after_name", myPlaylistsResponse.playlists.first().author)
    }

    private fun getSessionMemberCode(session: MockHttpSession): String {
        return session.getAttribute(SessionKeys.MEMBER_CODE) as String
    }
}
