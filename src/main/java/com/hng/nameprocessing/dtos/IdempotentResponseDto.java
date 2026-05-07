package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@JsonPropertyOrder({"status", "message", "data"})
@AllArgsConstructor
@NoArgsConstructor
public class IdempotentResponseDto implements ApiResponse, Serializable {
  @JsonProperty("status")
  private String status;

  @JsonProperty("message")
  private String message;

  @JsonProperty("data")
  private DataDto data;
}
