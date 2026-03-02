package com.garfield.myplaylists.adapter.`in`.web.model.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank(message = "닉네임을 입력해주세요.")
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요.")
    @field:Pattern(regexp = "^[a-zA-Z0-9가-힣_]+$", message = "닉네임은 한글/영문/숫자/_ 만 사용할 수 있습니다.")
    val nickname: String,
)
