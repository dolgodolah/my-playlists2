package com.garfield.myplaylists.adapter.`in`.web.model.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank(message = "회원코드를 입력해주세요.")
    @field:Size(max = 64, message = "회원코드는 최대 64자까지 입력할 수 있습니다.")
    @field:Pattern(regexp = "^[A-Za-z0-9]+$", message = "회원코드는 영문/숫자만 입력할 수 있습니다.")
    val memberCode: String,
)
