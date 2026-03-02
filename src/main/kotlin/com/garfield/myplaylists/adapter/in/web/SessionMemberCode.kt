package com.garfield.myplaylists.adapter.`in`.web

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SessionMemberCode(
    val required: Boolean = true,
)
