package com.hng.nameprocessing.security;

import com.hng.nameprocessing.services.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

  private final RateLimiterService rateLimiterService;

  public RateLimitingFilter(RateLimiterService rateLimiterService) {
    this.rateLimiterService = rateLimiterService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    String key;
    int limit;

    if (path.startsWith("/auth")) {
      key = request.getRemoteAddr();
      limit = 10;
    } else {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();

      if (auth != null && auth.isAuthenticated()) {
        key = auth.getName();
      } else {
        key = request.getRemoteAddr();
      }

      limit = 60;
    }

    String bucketKey = path + ":" + key;

    if (!rateLimiterService.isAllowed(bucketKey, limit)) {
      response.setStatus(429);
      response.setContentType("application/json");
      response
          .getWriter()
          .write(
              """
                {"error":"Too Many Requests"}
            """);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
