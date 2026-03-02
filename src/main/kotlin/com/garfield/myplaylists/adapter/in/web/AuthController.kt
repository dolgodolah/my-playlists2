package com.garfield.myplaylists.adapter.`in`.web

import com.garfield.myplaylists.adapter.`in`.web.model.request.LoginRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.SignupRequest
import com.garfield.myplaylists.adapter.`in`.web.model.request.UpdateNicknameRequest
import com.garfield.myplaylists.adapter.`in`.web.model.response.SessionMemberResponse
import com.garfield.myplaylists.adapter.`in`.web.model.response.SignupMemberResponse
import com.garfield.myplaylists.domain.auth.Member
import com.garfield.myplaylists.usecase.port.`in`.MemberUseCase
import com.garfield.myplaylists.usecase.port.`in`.LoginUseCase
import com.garfield.myplaylists.usecase.port.`in`.SignupUseCase
import com.garfield.myplaylists.usecase.port.`in`.UpdateMyNicknameUseCase
import com.garfield.myplaylists.usecase.port.`in`.command.LoginCommand
import com.garfield.myplaylists.usecase.port.`in`.command.SignupCommand
import com.garfield.myplaylists.usecase.port.`in`.command.UpdateMyNicknameCommand
import com.garfield.myplaylists.usecase.port.`in`.query.GetMemberQuery
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val signupUseCase: SignupUseCase,
    private val loginUseCase: LoginUseCase,
    private val memberUseCase: MemberUseCase,
    private val updateMyNicknameUseCase: UpdateMyNicknameUseCase,
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): SignupMemberResponse {
        val member = signupUseCase.execute(
            SignupCommand(nickname = request.nickname)
        )
        return member.toSignupResponse()
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        session: HttpSession,
    ): SessionMemberResponse {
        val member = loginUseCase.execute(
            LoginCommand(memberCode = request.memberCode)
        )
        session.setAttribute(SessionKeys.MEMBER_CODE, member.memberCode)
        return member.toSessionResponse()
    }

    @PostMapping("/logout")
    fun logout(session: HttpSession): ResponseEntity<Void> {
        session.invalidate()
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/session")
    fun session(@SessionMemberCode memberCode: String): SessionMemberResponse {
        val member = memberUseCase.execute(
            GetMemberQuery(memberCode = memberCode)
        )
        return member.toSessionResponse()
    }

    @PutMapping("/me")
    fun updateMyNickname(
        @Valid @RequestBody request: UpdateNicknameRequest,
        @SessionMemberCode memberCode: String,
    ): SessionMemberResponse {
        val updatedMember = updateMyNicknameUseCase.execute(
            UpdateMyNicknameCommand(
                memberCode = memberCode,
                nickname = request.nickname,
            )
        )

        return updatedMember.toSessionResponse()
    }

    private fun Member.toSignupResponse() = SignupMemberResponse(
        issuedCode = this.memberCode,
        nickname = this.nickname,
    )

    private fun Member.toSessionResponse() = SessionMemberResponse(
        nickname = this.nickname,
    )

}
