package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NationalizeResponse {
    @JsonProperty("count")
    private final Long count;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("country")
    private final List<Country> countryList;
}
