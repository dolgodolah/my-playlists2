package com.garfield.myplaylists.adapter.out.persistence

import com.garfield.myplaylists.adapter.out.persistence.entity.PlaylistEntity
import com.garfield.myplaylists.adapter.out.persistence.mapper.toDomain
import com.garfield.myplaylists.domain.auth.Member
import com.garfield.myplaylists.domain.playlist.Playlist
import com.garfield.myplaylists.usecase.port.out.MemberRepository
import com.garfield.myplaylists.usecase.port.out.PlaylistRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class InMemoryPlaylistRepository(
    private val memberRepository: MemberRepository,
) : PlaylistRepository {
    private val sequence = AtomicLong(1000)
    private val playlistsById = ConcurrentHashMap<Long, PlaylistEntity>()

    init {
        seedPublicPlaylists()
    }

    override fun createStarterPlaylistForMember(member: Member) {
        val exists = playlistsById.values.any { it.memberCode == member.memberCode }
        if (exists) {
            return
        }

        val id = sequence.incrementAndGet()
        playlistsById[id] = PlaylistEntity(
            playlistId = id,
            memberCode = member.memberCode,
            title = "나의 첫 플레이리스트",
            description = "좋아하는 곡을 하나씩 추가해보세요.",
            updatedAt = LocalDateTime.now(),
            visibility = true,
            songCount = 0,
        )
    }

    override fun createPlaylist(member: Member, title: String, description: String, visibility: Boolean): Playlist {
        val id = sequence.incrementAndGet()
        val entity = PlaylistEntity(
            playlistId = id,
            memberCode = member.memberCode,
            title = title,
            description = description,
            updatedAt = LocalDateTime.now(),
            visibility = visibility,
            songCount = 0,
        )
        playlistsById[id] = entity
        return entity.toDomain()
    }

    override fun findByPlaylistId(playlistId: Long): Playlist? = playlistsById[playlistId]?.toDomain()

    override fun increaseSongCount(playlistId: Long): Playlist? {
        val current = playlistsById[playlistId] ?: return null
        val updated = current.copy(
            songCount = current.songCount + 1,
            updatedAt = LocalDateTime.now(),
        )
        playlistsById[playlistId] = updated
        return updated.toDomain()
    }

    override fun decreaseSongCount(playlistId: Long): Playlist? {
        val current = playlistsById[playlistId] ?: return null
        val updated = current.copy(
            songCount = (current.songCount - 1).coerceAtLeast(0),
            updatedAt = LocalDateTime.now(),
        )
        playlistsById[playlistId] = updated
        return updated.toDomain()
    }

    override fun updatePlaylist(playlistId: Long, title: String, description: String, visibility: Boolean): Playlist? {
        val current = playlistsById[playlistId] ?: return null
        val updated = current.copy(
            title = title,
            description = description,
            visibility = visibility,
            updatedAt = LocalDateTime.now(),
        )
        playlistsById[playlistId] = updated
        return updated.toDomain()
    }

    override fun deletePlaylist(playlistId: Long): Boolean = playlistsById.remove(playlistId) != null

    override fun findByOwnerMemberCode(memberCode: String, lastPlaylistId: Long?, limit: Int): List<Playlist> {
        return playlistsById.values
            .asSequence()
            .filter { it.memberCode == memberCode }
            .filter { lastPlaylistId == null || it.playlistId < lastPlaylistId }
            .sortedByDescending { it.playlistId }
            .take(limit)
            .map { it.toDomain() }
            .toList()
    }

    override fun findPublicPlaylists(lastPlaylistId: Long?, limit: Int): List<Playlist> {
        return playlistsById.values
            .asSequence()
            .filter { it.visibility }
            .filter { lastPlaylistId == null || it.playlistId < lastPlaylistId }
            .sortedByDescending { it.playlistId }
            .take(limit)
            .map { it.toDomain() }
            .toList()
    }

    private fun seedPublicPlaylists() {
        val now = LocalDateTime.now()
        val seedItems = listOf(
            SeedPlaylist("PUBLIC_EDITOR_1", "캔디", "개발 집중 플레이리스트", "집중할 때 듣는 곡 모음", 12),
            SeedPlaylist("PUBLIC_EDITOR_2", "민트", "퇴근길 플레이리스트", "저녁 시간에 듣기 좋은 곡 모음", 18),
            SeedPlaylist("PUBLIC_EDITOR_3", "코코", "새벽 감성 플레이리스트", "새벽 공기와 잘 어울리는 곡", 24),
            SeedPlaylist("PUBLIC_EDITOR_4", "루나", "헬스장 에너지 플레이리스트", "운동 텐션을 올려주는 곡", 31),
            SeedPlaylist("PUBLIC_EDITOR_5", "하늘", "드라이브 플레이리스트", "고속도로에서 듣기 좋은 곡", 27),
            SeedPlaylist("PUBLIC_EDITOR_6", "레오", "비 오는 날 플레이리스트", "잔잔한 분위기의 곡 모음", 19),
            SeedPlaylist("PUBLIC_EDITOR_7", "모모", "주말 아침 플레이리스트", "느긋한 아침을 위한 곡", 16),
            SeedPlaylist("PUBLIC_EDITOR_8", "제이", "카페 작업 플레이리스트", "카페에서 작업할 때 좋은 곡", 22),
            SeedPlaylist("PUBLIC_EDITOR_9", "단비", "기분전환 플레이리스트", "리프레시가 필요한 날의 곡", 14),
            SeedPlaylist("PUBLIC_EDITOR_10", "태오", "밤 산책 플레이리스트", "밤길과 어울리는 곡", 20),
            SeedPlaylist("PUBLIC_EDITOR_11", "보라", "출근길 플레이리스트", "아침 이동 시간용 곡", 17),
            SeedPlaylist("PUBLIC_EDITOR_12", "우디", "독서 배경 플레이리스트", "독서할 때 방해 없는 곡", 13),
            SeedPlaylist("PUBLIC_EDITOR_13", "소이", "여행 준비 플레이리스트", "여행 전 설렘을 올리는 곡", 26),
            SeedPlaylist("PUBLIC_EDITOR_14", "하쿠", "집중 코딩 플레이리스트 2", "몰입 구간에 맞춘 트랙", 29),
            SeedPlaylist("PUBLIC_EDITOR_15", "리안", "노을 시간 플레이리스트", "노을 보며 듣기 좋은 곡", 15),
            SeedPlaylist("PUBLIC_EDITOR_16", "세나", "점심 산책 플레이리스트", "가볍게 걷기 좋은 리듬", 11),
            SeedPlaylist("PUBLIC_EDITOR_17", "도윤", "주말 운동 플레이리스트", "러닝 템포에 맞춘 트랙", 23),
            SeedPlaylist("PUBLIC_EDITOR_18", "아린", "감성 발라드 플레이리스트", "잔잔하게 몰입되는 발라드", 28),
            SeedPlaylist("PUBLIC_EDITOR_19", "현우", "전자음악 플레이리스트", "비트 중심의 일렉트로닉", 21),
            SeedPlaylist("PUBLIC_EDITOR_20", "나래", "힐링 어쿠스틱 플레이리스트", "편안한 기타/보컬 모음", 14),
            SeedPlaylist("PUBLIC_EDITOR_21", "지후", "파티 스타터 플레이리스트", "분위기를 올리는 트랙", 32),
            SeedPlaylist("PUBLIC_EDITOR_22", "유진", "비 오는 밤 플레이리스트", "빗소리와 어울리는 곡", 18),
            SeedPlaylist("PUBLIC_EDITOR_23", "시온", "레트로 팝 플레이리스트", "복고 감성의 팝 트랙", 20),
            SeedPlaylist("PUBLIC_EDITOR_24", "채린", "아침 스트레칭 플레이리스트", "부드럽게 몸 푸는 음악", 12),
            SeedPlaylist("PUBLIC_EDITOR_25", "민재", "주간 결산 플레이리스트", "한 주 마무리에 듣는 곡", 24),
        )

        seedItems.forEachIndexed { index, item ->
            saveSeedMemberIfAbsent(memberCode = item.memberCode, nickname = item.nickname)
            val id = sequence.incrementAndGet()
            playlistsById[id] = PlaylistEntity(
                playlistId = id,
                memberCode = item.memberCode,
                title = item.title,
                description = item.description,
                updatedAt = now.minusHours((index + 1).toLong()),
                visibility = true,
                songCount = item.songCount,
            )
        }
    }

    private fun saveSeedMemberIfAbsent(memberCode: String, nickname: String) {
        if (!memberRepository.existsByMemberCode(memberCode)) {
            memberRepository.save(
                Member(
                    memberCode = memberCode,
                    nickname = nickname,
                ),
            )
        }
    }

    private data class SeedPlaylist(
        val memberCode: String,
        val nickname: String,
        val title: String,
        val description: String,
        val songCount: Int,
    )
}
