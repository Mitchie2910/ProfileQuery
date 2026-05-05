package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonPropertyOrder({"self", "next", "prev"})
@AllArgsConstructor
@NoArgsConstructor
public class Links {
  @JsonProperty("self")
  String self;

  @JsonProperty("next")
  String next;

  @JsonProperty("prev")
  String prev;
}
