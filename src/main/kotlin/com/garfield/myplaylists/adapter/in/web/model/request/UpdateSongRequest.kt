package com.garfield.myplaylists.adapter.`in`.web.model.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateSongRequest(
    @field:NotBlank(message = "노래 제목을 입력해주세요.")
    @field:Size(max = 50, message = "노래 제목은 최대 50자까지 입력할 수 있습니다.")
    val title: String,
    @field:Size(max = 100, message = "노래 설명은 최대 100자까지 입력할 수 있습니다.")
    val description: String = "",
)
