package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
@Builder
@JsonPropertyOrder({"id", "name", "gender", "age", "age_group", "country_id"})
public class DataRepositoryDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("age")
    private Integer age;

    @JsonProperty("age_group")
    private String ageGroup;

    @JsonProperty("country_id")
    private String countryId;

    public DataRepositoryDto(@JsonProperty("id") UUID id,
                             @JsonProperty("name") String name,
                             @JsonProperty("gender") String gender,
                             @JsonProperty("age") Integer age,
                             @JsonProperty("age_group") String ageGroup,
                             @JsonProperty("country_id") String countryId) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.ageGroup = ageGroup;
        this.countryId = countryId;
    }
}
