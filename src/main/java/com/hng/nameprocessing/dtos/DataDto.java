package com.hng.nameprocessing.dtos;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"id", "name", "gender", "gender_probability", "sample_size", "age", "age_group", "country_id", "country_name", "country_probability", "created_at"})
public class DataDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("gender_probability")
    private Double genderProbability;

    @JsonProperty("age")
    private int age;

    @JsonProperty("age_group")
    private String ageGroup;

    @JsonProperty("country_id")
    private String countryId;

    @JsonProperty("country_name")
    private String countryName;

    @JsonProperty("country_probability")
    private Double countryProbability;

    @JsonProperty("created_at")
    private Instant createdAt;
}
