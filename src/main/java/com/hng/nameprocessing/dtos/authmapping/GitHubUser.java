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
public class GitHubUser {
  @JsonProperty("login")
  String login;

  @JsonProperty("id")
  Long id;

  @JsonProperty("avatar_url")
  String avatarUrl;

  @JsonProperty("name")
  String name;

  @JsonProperty("email")
  String email;
}
