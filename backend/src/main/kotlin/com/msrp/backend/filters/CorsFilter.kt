package com.msrp.backend.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorsFilter(
    @Value("\${cors.allowed.origins}") allowedOriginsRaw: String,
) : OncePerRequestFilter() {
    private val allowedOrigins = allowedOriginsRaw.split(",").map { it.trim() }.toSet()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val origin = request.getHeader("Origin")
        if (origin != null && origin in allowedOrigins) {
            response.setHeader("Access-Control-Allow-Origin", origin)
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS")
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Admin-Key")
            response.setHeader("Access-Control-Max-Age", "3600")
        }

        if ("OPTIONS" == request.method) {
            response.status = HttpServletResponse.SC_OK
        } else {
            filterChain.doFilter(request, response)
        }
    }
}
