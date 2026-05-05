package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@JsonPropertyOrder({
  "id",
  "name",
  "gender",
  "gender_probability",
  "age",
  "age_group",
  "country_id",
  "country_name",
  "country_probability",
  "created_at"
})
@AllArgsConstructor
@NoArgsConstructor
public class DataRepositoryDto {
  ;
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

  @JsonProperty("gender_probability")
  private Double genderProbability;

  @JsonProperty("country_name")
  private String countryName;

  @JsonProperty("country_probability")
  private Double countryProbability;

  @JsonProperty("created_at")
  private Instant createdAt;

  public DataRepositoryDto(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("gender") String gender,
      @JsonProperty("age") Integer age,
      @JsonProperty("age_group") String ageGroup,
      @JsonProperty("country_id") String countryId,
      @JsonProperty("country_name") String countryName,
      @JsonProperty("country_probability") Double countryProbability,
      @JsonProperty("created_at") Instant createdAt,
      @JsonProperty("gender_probability") Double genderProbability) {
    this.id = id;
    this.name = name;
    this.gender = gender;
    this.age = age;
    this.ageGroup = ageGroup;
    this.countryId = countryId;
    this.countryName = countryName;
    this.countryProbability = countryProbability;
    this.createdAt = createdAt;
    //        this.genderProbability = genderProbability;
  }
}
