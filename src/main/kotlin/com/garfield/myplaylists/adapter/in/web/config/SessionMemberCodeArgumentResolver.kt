package com.garfield.myplaylists.adapter.`in`.web.config

import com.garfield.myplaylists.adapter.`in`.web.SessionKeys
import com.garfield.myplaylists.adapter.`in`.web.SessionMemberCode
import com.garfield.myplaylists.usecase.UnauthorizedException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class SessionMemberCodeArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(SessionMemberCode::class.java) &&
            String::class.java == parameter.parameterType
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val annotation = parameter.getParameterAnnotation(SessionMemberCode::class.java)
            ?: return null
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: return null
        val session = request.getSession(false)
        val memberCode = session?.getAttribute(SessionKeys.MEMBER_CODE) as? String

        if (annotation.required && memberCode == null) {
            throw UnauthorizedException("로그인이 필요합니다.")
        }
        return memberCode
    }
}
