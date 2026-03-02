package com.garfield.myplaylists.usecase.port.`in`

import com.garfield.myplaylists.domain.auth.Member
import com.garfield.myplaylists.usecase.UnauthorizedException
import com.garfield.myplaylists.usecase.port.`in`.command.LoginCommand
import com.garfield.myplaylists.usecase.port.out.MemberRepository
import org.springframework.stereotype.Service

@Service
class LoginUseCase(
    private val memberRepository: MemberRepository,
) {
    fun execute(command: LoginCommand): Member {
        return memberRepository.findByMemberCode(command.memberCode)
            ?: throw UnauthorizedException("유효하지 않은 회원코드입니다.")
    }
}
