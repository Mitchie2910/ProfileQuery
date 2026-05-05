package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AgifyResponse {
  @JsonProperty("count")
  private final Long count;

  @JsonProperty("name")
  private final String name;

  @JsonProperty("age")
  private final Integer age;
}
