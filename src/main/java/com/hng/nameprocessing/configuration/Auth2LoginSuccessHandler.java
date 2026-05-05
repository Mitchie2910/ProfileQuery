package com.hng.nameprocessing.configuration;

import com.hng.nameprocessing.dtos.AuthContext;
import com.hng.nameprocessing.dtos.User;
import com.hng.nameprocessing.security.JwtService;
import com.hng.nameprocessing.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class Auth2LoginSuccessHandler implements AuthenticationSuccessHandler {
  private final UserService userService;
  private final JwtService jwtService;

  @Value("${app.frontend.url}")
  private String frontendUrl;

  public Auth2LoginSuccessHandler(UserService userService, JwtService jwtService) {
    this.userService = userService;
    this.jwtService = jwtService;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;

    //        System.out.println("Authorities: " + authentication.getAuthorities());

    OAuth2User oauthUser = token.getPrincipal();

    java.lang.Object idObj = oauthUser.getAttribute("id");
    String githubId = String.valueOf(idObj);
    String userName = oauthUser.getAttribute("username");
    String email = oauthUser.getAttribute("email");
    String avatarUrl = oauthUser.getAttribute("avatar_url");

    User user = userService.findOrCreateUser(githubId, userName, email, avatarUrl);

    if (!user.isActive()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    AuthContext ctx = new AuthContext(user.getGithubId(), user.getRole());

    boolean isBrowser =
        request.getHeader("User-Agent") != null
            && request.getHeader("User-Agent").contains("Mozilla");

    if (isBrowser) {

      response.sendRedirect(frontendUrl + "/dashboard");

      //            String accessToken = jwtService.generateAccessToken(ctx);
      //            String refreshToken = jwtService.generateRefreshToken(ctx);
      //            System.out.println(accessToken);
      //            System.out.println(refreshToken);
    }
  }
}
