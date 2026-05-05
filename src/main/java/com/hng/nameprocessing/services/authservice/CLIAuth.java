package com.hng.nameprocessing.services.authservice;

import com.hng.nameprocessing.dtos.AuthContext;
import com.hng.nameprocessing.dtos.User;
import com.hng.nameprocessing.dtos.authmapping.AuthResponse;
import com.hng.nameprocessing.security.JwtService;
import com.hng.nameprocessing.services.UserService;
import com.hng.nameprocessing.services.restclients.GitHubClient;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CLIAuth {
  private final UserService userService;
  private final JwtService jwtService;
  private final GitHubClient gitHubClient;
  private final Executor executor;

  @Value("${app.security.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  public CLIAuth(
      UserService userService,
      JwtService jwtService,
      GitHubClient gitHubClient,
      @Qualifier("asyncExecutor") Executor executor) {
    this.userService = userService;
    this.jwtService = jwtService;
    this.gitHubClient = gitHubClient;
    this.executor = executor;
  }

  public CompletableFuture<AuthResponse> authenticate(String code, String codeVerifier) {
    return gitHubClient
        .exchangeCode(code, codeVerifier)
        .thenCompose(gitHubClient::fetchUser)
        .thenApply(
            gitHubUser -> {
              User user = userService.findOrCreateUser(gitHubUser);
              AuthContext ctx = new AuthContext(user.getGithubId(), user.getRole());

              String accessToken = jwtService.generateAccessToken(ctx);
              String refreshToken = jwtService.generateRefreshToken(ctx);

              return AuthResponse.builder()
                  .accessToken(accessToken)
                  .refreshToken(refreshToken)
                  .userName(user.getUsername())
                  .expiresAt(Instant.now().plusMillis(accessTokenExpiration))
                  .build();
            });
  }

  public CompletableFuture<AuthResponse> refresh(String refreshToken) {
    return CompletableFuture.supplyAsync(() -> jwtService.refreshTokens(refreshToken), executor);
  }
}
