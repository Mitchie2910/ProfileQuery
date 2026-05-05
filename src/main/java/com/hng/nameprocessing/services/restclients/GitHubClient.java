package com.hng.nameprocessing.services.restclients;

import com.hng.nameprocessing.dtos.authmapping.GitHubUser;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class GitHubClient {

  private final RestClient restClient;
  private final Executor executor;

  @Value("${spring.security.oauth2.client.registration.github.clientId}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.github.clientSecret}")
  private String clientSecret;

  public GitHubClient(
      RestClient.Builder restClientBuilder, @Qualifier("asyncExecutor") Executor executor) {
    this.restClient =
        restClientBuilder
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    this.executor = executor;
  }

  //    public CompletableFuture<String> exchangeCode(String code, String codeVerifier) {
  //        return CompletableFuture.supplyAsync(()->{
  //            @Nullable Map response = restClient.post()
  //                    .uri(uriBuilder -> uriBuilder
  //                            .("https://github.com/login/oauth/access_token")
  //                            .queryParam("client_id", clientId)
  //                            .queryParam("client_secret", clientSecret)
  //                            .queryParam("code", code)
  //                            .queryParam("code_verifier", codeVerifier)
  //                            .build())
  //                    .retrieve()
  //                    .body(Map.class);
  //
  //            return (String) response.get("access_token");
  //        },executor );
  //
  //    }

  public CompletableFuture<String> exchangeCode(String code, String codeVerifier) {
    return CompletableFuture.supplyAsync(
        () -> {
          MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
          form.add("client_id", clientId);
          form.add("client_secret", clientSecret);
          form.add("code", code);
          form.add("code_verifier", codeVerifier);

          Map response =
              restClient
                  .post()
                  .uri("https://github.com/login/oauth/access_token")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .accept(MediaType.APPLICATION_JSON) // important
                  .body(form)
                  .retrieve()
                  .body(Map.class);

          return (String) response.get("access_token");
        },
        executor);
  }

  public CompletableFuture<GitHubUser> fetchUser(String accessToken) {

    return CompletableFuture.supplyAsync(
        () ->
            restClient
                .get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GitHubUser.class),
        executor);
  }
}
