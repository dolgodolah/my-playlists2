package com.garfield.myplaylists.adapter.out.persistence.mapper

import com.garfield.myplaylists.adapter.out.persistence.entity.MemberEntity
import com.garfield.myplaylists.adapter.out.persistence.entity.PlaylistEntity
import com.garfield.myplaylists.adapter.out.persistence.entity.SongEntity
import com.garfield.myplaylists.domain.auth.Member
import com.garfield.myplaylists.domain.playlist.Playlist
import com.garfield.myplaylists.domain.song.Song

fun Member.toEntity(): MemberEntity = MemberEntity(
    memberCode = memberCode,
    nickname = nickname,
)

fun MemberEntity.toDomain(): Member = Member(
    memberCode = memberCode,
    nickname = nickname,
)

fun PlaylistEntity.toDomain(): Playlist = Playlist(
    playlistId = playlistId,
    memberCode = memberCode,
    title = title,
    description = description,
    updatedAt = updatedAt,
    visibility = visibility,
    songCount = songCount,
)

fun SongEntity.toDomain(): Song = Song(
    songId = songId,
    playlistId = playlistId,
    memberCode = memberCode,
    title = title,
    videoId = videoId,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
