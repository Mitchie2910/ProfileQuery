package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenderizeResponse {
  @JsonProperty("count")
  private final Long count;

  @JsonProperty("name")
  private final String name;

  @JsonProperty("gender")
  private final String gender;

  @JsonProperty("probability")
  private final Double probability;
}
