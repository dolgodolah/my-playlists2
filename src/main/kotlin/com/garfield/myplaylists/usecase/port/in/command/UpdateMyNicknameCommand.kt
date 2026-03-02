package com.garfield.myplaylists.usecase.port.`in`.command

data class UpdateMyNicknameCommand(
    val memberCode: String,
    val nickname: String,
)
