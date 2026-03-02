package com.garfield.myplaylists.usecase.port.`in`

import com.garfield.myplaylists.domain.auth.Member
import com.garfield.myplaylists.usecase.port.`in`.command.SignupCommand
import com.garfield.myplaylists.usecase.port.out.MemberRepository
import com.garfield.myplaylists.usecase.port.out.PlaylistRepository
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class SignupUseCase(
    private val memberRepository: MemberRepository,
    private val playlistRepository: PlaylistRepository,
) {

    private val random = SecureRandom()

    companion object {
        private const val MAX_MEMBER_CODE_GENERATION_ATTEMPTS = 3
        private const val MEMBER_CODE_LENGTH = 12
        private const val MEMBER_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }

    fun execute(command: SignupCommand): Member {
        if (memberRepository.existsByNickname(command.nickname)) {
            throw IllegalArgumentException("이미 사용 중인 닉네임입니다.")
        }

        val memberCode = generateUniqueMemberCode()
        val member = Member(memberCode = memberCode, nickname = command.nickname)

        val savedMember = memberRepository.save(member)
        playlistRepository.createStarterPlaylistForMember(savedMember)

        return savedMember
    }

    private fun generateUniqueMemberCode(): String {
        repeat(MAX_MEMBER_CODE_GENERATION_ATTEMPTS) {
            val memberCode = buildString(MEMBER_CODE_LENGTH) {
                repeat(MEMBER_CODE_LENGTH) {
                    append(MEMBER_CODE_CHARS[random.nextInt(MEMBER_CODE_CHARS.length)])
                }
            }

            if (!memberRepository.existsByMemberCode(memberCode)) {
                return memberCode
            }
        }

        throw IllegalStateException("회원코드 생성에 실패했습니다. 다시 시도해주세요.")
    }
}
