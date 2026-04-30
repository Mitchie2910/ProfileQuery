package com.hng.nameprocessing.configuration.components;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiVersionFilter extends OncePerRequestFilter {

    private static final String REQUIRED_VERSION = "1";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (uri.startsWith("/api/profiles")) {

            String version = request.getHeader("X-API-Version");

            if (version == null || !version.equals(REQUIRED_VERSION)) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");

                response.getWriter().write("""
                    {
                      "status": "error",
                      "message": "API version header required"
                    }
                """);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
