package com.hng.nameprocessing.configuration;

import com.hng.nameprocessing.repositories.UserRepository;
import com.hng.nameprocessing.security.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

  private final JwtService jwtService;
  private final UserRepository userRepository;

  public CustomLogoutSuccessHandler(JwtService jwtService, UserRepository userRepository) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
  }

  @Override
  public void onLogoutSuccess(
      final HttpServletRequest request,
      final HttpServletResponse response,
      @Nullable Authentication authentication)
      throws IOException, ServletException {
    String refreshToken = extractRefreshToken(request);

    if (refreshToken != null) {
      jwtService.invalidateToken(refreshToken);
    }
    response.setStatus(HttpServletResponse.SC_OK);
  }

  private String extractRefreshToken(HttpServletRequest request) {
    String refreshToken = request.getHeader("Authorization");

    if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
      refreshToken = refreshToken.substring(7);
    }
    return refreshToken;
  }
}
