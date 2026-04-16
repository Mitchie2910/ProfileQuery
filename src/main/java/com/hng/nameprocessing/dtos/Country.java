package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Country {

    @JsonProperty("country_id")
    private final String countryId;

    @JsonProperty("probability")
    private final Double probability;
}
