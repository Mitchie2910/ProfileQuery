package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class NationalizeResponse {
  @JsonProperty("count")
  private final Long count;

  @JsonProperty("name")
  private final String name;

  @JsonProperty("country")
  private final List<Country> countryList;
}
