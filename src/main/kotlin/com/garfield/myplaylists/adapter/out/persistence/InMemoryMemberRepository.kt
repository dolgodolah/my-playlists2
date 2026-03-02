package com.garfield.myplaylists.adapter.out.persistence

import com.garfield.myplaylists.adapter.out.persistence.entity.MemberEntity
import com.garfield.myplaylists.adapter.out.persistence.mapper.toDomain
import com.garfield.myplaylists.adapter.out.persistence.mapper.toEntity
import com.garfield.myplaylists.domain.auth.Member
import com.garfield.myplaylists.usecase.port.out.MemberRepository
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryMemberRepository : MemberRepository {
    private val membersByCode = ConcurrentHashMap<String, MemberEntity>()
    private val codeByNickname = ConcurrentHashMap<String, String>()

    override fun existsByNickname(nickname: String): Boolean = codeByNickname.containsKey(nickname)

    override fun save(member: Member): Member {
        val entity = member.toEntity()
        membersByCode[entity.memberCode] = entity
        codeByNickname[entity.nickname] = entity.memberCode
        return entity.toDomain()
    }

    override fun findByMemberCode(memberCode: String): Member? = membersByCode[memberCode]?.toDomain()

    override fun existsByMemberCode(memberCode: String): Boolean = membersByCode.containsKey(memberCode)

    override fun updateNickname(memberCode: String, nickname: String): Member? {
        val memberEntity = membersByCode[memberCode] ?: return null

        if (memberEntity.nickname != nickname) {
            codeByNickname.remove(memberEntity.nickname)
        }

        val updatedEntity = memberEntity.copy(nickname = nickname)
        membersByCode[memberCode] = updatedEntity
        codeByNickname[nickname] = memberCode

        return updatedEntity.toDomain()
    }
}
