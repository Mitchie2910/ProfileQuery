package com.hng.nameprocessing.dtos.authmapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LoginRequest {

  @JsonProperty("code")
  String code;

  @JsonProperty("code_verifier")
  String codeVerifier;
}
