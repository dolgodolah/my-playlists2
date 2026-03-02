package com.garfield.myplaylists.usecase.port.`in`

import com.garfield.myplaylists.domain.auth.Member
import com.garfield.myplaylists.usecase.UnauthorizedException
import com.garfield.myplaylists.usecase.port.`in`.command.UpdateMyNicknameCommand
import com.garfield.myplaylists.usecase.port.out.MemberRepository
import com.garfield.myplaylists.usecase.port.out.PlaylistRepository
import org.springframework.stereotype.Service

@Service
class UpdateMyNicknameUseCase(
    private val memberRepository: MemberRepository,
    private val playlistRepository: PlaylistRepository,
) {
    fun execute(command: UpdateMyNicknameCommand): Member {
        val currentMember = memberRepository.findByMemberCode(command.memberCode)
            ?: throw UnauthorizedException("로그인 세션이 유효하지 않습니다.")

        val newNickname = command.nickname
        if (currentMember.nickname == newNickname) {
            return currentMember
        }

        if (memberRepository.existsByNickname(newNickname)) {
            throw IllegalArgumentException("이미 사용 중인 닉네임입니다.")
        }

        val updatedMember = memberRepository.updateNickname(command.memberCode, newNickname)
            ?: throw UnauthorizedException("로그인 세션이 유효하지 않습니다.")

        return updatedMember
    }
}
