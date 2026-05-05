package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonPropertyOrder({"status", "data"})
@AllArgsConstructor
@NoArgsConstructor
public class FreshResponseDto implements ApiResponse {
  @JsonProperty("status")
  private String status;

  @JsonProperty("data")
  private DataDto data;
}
