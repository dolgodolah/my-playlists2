package com.garfield.myplaylists.adapter.`in`.web.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.method.support.HandlerMethodArgumentResolver

@Configuration
class WebConfig(
    @Value("\${app.cors.allowed-origins:}")
    private val allowedOriginsRaw: String,
    private val apiLoggingInterceptor: ApiLoggingInterceptor,
    private val sessionMemberCodeArgumentResolver: SessionMemberCodeArgumentResolver,
) : WebMvcConfigurer {
    private val allowedOrigins: Array<String>
        get() = allowedOriginsRaw
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toTypedArray()

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns(*allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiLoggingInterceptor)
            .addPathPatterns("/api/**")
    }

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(sessionMemberCodeArgumentResolver)
    }
}
