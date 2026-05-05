package com.hng.nameprocessing.services.restclients;

import com.hng.nameprocessing.dtos.GenderizeResponse;
import com.hng.nameprocessing.exceptions.ExternalApiException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GenderizeClient {
  private final RestClient restClient;
  private final Executor executor;

  Logger LOGGER = LoggerFactory.getLogger(GenderizeClient.class);

  public GenderizeClient(
      RestClient.Builder restClientBuilder, @Qualifier("asyncExecutor") Executor executor) {
    this.restClient = restClientBuilder.baseUrl("https://api.genderize.io").build();
    this.executor = executor;
  }

  @SneakyThrows
  public CompletableFuture<GenderizeResponse> genderizeRequest(String name) {
    LOGGER.info("GENDERIZE STARTING");
    return CompletableFuture.supplyAsync(
        () ->
            restClient
                .get()
                .uri(uriBuilder -> uriBuilder.queryParam("name", name).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::errorHandler)
                .body(GenderizeResponse.class),
        executor);
  }

  private void errorHandler(HttpRequest request, ClientHttpResponse response) throws IOException {

    LOGGER.error("REST CLIENT ERROR");
    switch (response.getStatusCode().value()) {
      case 400:
      //                throw new MissingNameException(response.getStatusCode().value(), "Name
      // parameter is missing");
      case 422:
        throw new ExternalApiException("Invalid name parameter", 500);
      case 429:
        throw new ExternalApiException("Request limit reached", 500);
    }
  }
}
