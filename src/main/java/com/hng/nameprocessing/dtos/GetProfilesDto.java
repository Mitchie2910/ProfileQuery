package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@JsonPropertyOrder({"status", "count", "data"})
@AllArgsConstructor
@NoArgsConstructor
public class GetProfilesDto {
    @JsonProperty("status")
    private String status;

    @JsonProperty("count")
    private int count;

    @JsonProperty("data")
    private List<DataRepositoryDto> data;
}
