package com.garfield.myplaylists.usecase.port.out

import com.garfield.myplaylists.domain.auth.Member

interface MemberRepository {
    fun existsByNickname(nickname: String): Boolean
    fun save(member: Member): Member
    fun findByMemberCode(memberCode: String): Member?
    fun existsByMemberCode(memberCode: String): Boolean
    fun updateNickname(memberCode: String, nickname: String): Member?
}
