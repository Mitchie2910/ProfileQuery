package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserDto {
  @JsonProperty("github_id")
  String githubId;

  @JsonProperty("username")
  String username;

  @JsonProperty("email")
  String email;

  @JsonProperty("role")
  String role;
}
