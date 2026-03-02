package com.garfield.myplaylists.usecase.port.`in`

import com.garfield.myplaylists.domain.auth.Member
import com.garfield.myplaylists.usecase.UnauthorizedException
import com.garfield.myplaylists.usecase.port.`in`.query.GetMemberQuery
import com.garfield.myplaylists.usecase.port.out.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberUseCase(
    private val memberRepository: MemberRepository,
) {
    fun execute(query: GetMemberQuery): Member {
        return memberRepository.findByMemberCode(query.memberCode)
            ?: throw UnauthorizedException("유효하지 않은 회원코드입니다.")
    }
}
