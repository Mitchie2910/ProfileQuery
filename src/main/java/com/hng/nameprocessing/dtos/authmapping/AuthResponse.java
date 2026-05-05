package com.hng.nameprocessing.dtos.authmapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuthResponse {
  @JsonProperty("access_token")
  String accessToken;

  @JsonProperty("refresh_token")
  String refreshToken;

  @JsonProperty("username")
  String userName;

  @JsonProperty("expires_at")
  Instant expiresAt;
}
