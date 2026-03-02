package com.garfield.myplaylists.adapter.`in`.web.model.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdatePlaylistRequest(
    @field:NotBlank(message = "플레이리스트 타이틀을 입력해주세요.")
    @field:Size(min = 2, max = 50, message = "플레이리스트 타이틀은 2~50자로 입력해주세요.")
    val title: String,
    @field:Size(max = 100, message = "플레이리스트 소개는 최대 100자까지 입력할 수 있습니다.")
    val description: String = "",
    val visibility: Boolean,
)
