package com.hng.nameprocessing.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hng.nameprocessing.dtos.AuthContext;
import com.hng.nameprocessing.dtos.RefreshTokenMapping;
import com.hng.nameprocessing.dtos.Role;
import com.hng.nameprocessing.dtos.User;
import com.hng.nameprocessing.dtos.authmapping.AuthResponse;
import com.hng.nameprocessing.repositories.RefreshTokenRepository;
import com.hng.nameprocessing.services.UserService;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private static final String TOKEN_TYPE = "token_type";
  private final RSAPrivateKey privateKey;
  private final RSAPublicKey publicKey;
  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${app.security.jwt.access-token-expiration}")
  private long accessTokenExpiration;

  @Value("${app.security.jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  private UserService userService;

  public JwtService(RefreshTokenRepository refreshTokenRepository, UserService userService)
      throws Exception {
    this.privateKey = KeyUtils.loadPrivateKey("/keys/local-only/private_key.pem");
    this.publicKey = KeyUtils.loadPublicKey("/keys/local-only/public_key.pem");
    this.refreshTokenRepository = refreshTokenRepository;
    this.userService = userService;
  }

  public String generateAccessToken(final AuthContext ctx) {
    List<String> roles = List.of(ctx.role().name());

    final Map<String, Object> claims = Map.of(TOKEN_TYPE, "ACCESS_TOKEN", "role", roles);
    return buildToken(ctx.userId(), claims, this.accessTokenExpiration);
  }

  public String generateRefreshToken(final AuthContext ctx) {
    Algorithm algorithm = Algorithm.RSA256(null, privateKey);

    List<String> roles = List.of(ctx.role().name());

    final Map<String, Object> claims = Map.of(TOKEN_TYPE, "REFRESH_TOKEN", "role", roles);
    String jti = UUID.randomUUID().toString();

    String token =
        JWT.create()
            .withClaim("roles", (List<String>) claims.get("role"))
            .withClaim(TOKEN_TYPE, (String) claims.get(TOKEN_TYPE))
            .withSubject(ctx.userId())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plusMillis(this.refreshTokenExpiration))
            .withJWTId(jti)
            .sign(algorithm);

    RefreshTokenMapping refreshTokenMapping =
        RefreshTokenMapping.builder()
            .tokenId(jti)
            .revoked(false)
            .expiresAt(Instant.now().plusMillis(this.refreshTokenExpiration))
            .githubId(ctx.userId())
            .build();

    refreshTokenRepository.save(refreshTokenMapping);

    return token;
  }

  private String buildToken(
      final String githubId, final Map<String, Object> claims, final long tokenExpiration) {
    Algorithm algorithm = Algorithm.RSA256(null, privateKey);
    return JWT.create()
        .withClaim("roles", (List<String>) claims.get("role"))
        .withClaim(TOKEN_TYPE, (String) claims.get(TOKEN_TYPE))
        .withSubject(githubId)
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plusMillis(tokenExpiration))
        .sign(algorithm);
  }

  public boolean isTokenValid(final String token, final String expectedGithubId) {
    final String username = extractGithubId(token);
    return username.equals(expectedGithubId) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(final String token) {
    return extractClaims(token).getExpiresAt().before(new Date());
  }

  private String extractGithubId(final String token) {
    return extractClaims(token).getSubject();
  }

  private DecodedJWT extractClaims(final String token) {
    Algorithm algorithm = Algorithm.RSA256(publicKey, null);

    try {
      return JWT.require(algorithm).build().verify(token);

    } catch (JWTVerificationException e) {
      throw new RuntimeException("Invalid JWT token", e);
    }
  }

  public String getGithubId(final String refreshToken) {
    DecodedJWT jwt = extractClaims(refreshToken);
    return jwt.getSubject();
  }

  public void invalidateToken(final String refreshToken) {

    DecodedJWT jwt = extractClaims(refreshToken);

    String jti = jwt.getId();
    String githubId = jwt.getSubject();

    RefreshTokenMapping stored = refreshTokenRepository.findByTokenId(jti);

    if (stored.isRevoked()) {
      throw new RuntimeException("Token already used");
    }

    if (isTokenExpired(refreshToken)) {
      throw new RuntimeException("Token expired");
    }

    stored.setRevoked(true);
    refreshTokenRepository.save(stored);
  }

  public AuthResponse refreshTokens(final String refreshToken) {

    DecodedJWT jwt = extractClaims(refreshToken);

    String jti = jwt.getId();
    String githubId = jwt.getSubject();
    Map<String, Claim> claims = jwt.getClaims();

    String roleString = claims.get("roles").asString();

    Role role = Role.valueOf(roleString);

    RefreshTokenMapping stored = refreshTokenRepository.findByTokenId(jti);

    if (stored.isRevoked()) {
      throw new RuntimeException("Token already used");
    }

    if (isTokenExpired(refreshToken)) {
      throw new RuntimeException("Token expired");
    }

    AuthContext context = new AuthContext(githubId, role);

    String accessToken = generateAccessToken(context);
    String refreshTokenNew = generateRefreshToken(context);

    User user = userService.findUser(githubId);

    AuthResponse response =
        AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshTokenNew)
            .userName(user.getUsername())
            .expiresAt(Instant.now().plusMillis(this.accessTokenExpiration))
            .build();

    stored.setRevoked(true);
    refreshTokenRepository.save(stored);

    return response;
  }
}
