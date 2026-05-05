package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hng.nameprocessing.validation.BasicChecks;
import com.hng.nameprocessing.validation.FormatChecks;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class RequestModel {

  @NotBlank(message = "Name cannot be blank", groups = BasicChecks.class)
  @Pattern(
      regexp = "^[a-zA-Z]+$",
      message = "name must contain only letters",
      groups = FormatChecks.class)
  private String name;

  @JsonCreator
  public RequestModel(@JsonProperty("name") String name) {
    this.name = name;
  }
}
