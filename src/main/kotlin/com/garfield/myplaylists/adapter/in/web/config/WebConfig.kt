package com.garfield.myplaylists.adapter.`in`.web.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.method.support.HandlerMethodArgumentResolver

@Configuration
class WebConfig(
    @Value("\${app.cors.allowed-origin:http://localhost:3000}")
    private val allowedOrigin: String,
    private val apiLoggingInterceptor: ApiLoggingInterceptor,
    private val sessionMemberCodeArgumentResolver: SessionMemberCodeArgumentResolver,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigin)
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
