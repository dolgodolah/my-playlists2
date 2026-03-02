package com.garfield.myplaylists.adapter.`in`.web.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ApiLoggingInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis())
        log.info("[API][REQ] {} {}", request.method, request.requestURI)
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        val startTime = request.getAttribute(START_TIME_ATTRIBUTE) as? Long ?: System.currentTimeMillis()
        val durationMs = System.currentTimeMillis() - startTime

        if (ex != null) {
            log.error(
                "[API][RES] {} {} -> status={} ({}ms), error={}",
                request.method,
                request.requestURI,
                response.status,
                durationMs,
                ex.message,
            )
            return
        }

        log.info(
            "[API][RES] {} {} -> status={} ({}ms)",
            request.method,
            request.requestURI,
            response.status,
            durationMs,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApiLoggingInterceptor::class.java)
        private const val START_TIME_ATTRIBUTE = "api.log.startTime"
    }
}
