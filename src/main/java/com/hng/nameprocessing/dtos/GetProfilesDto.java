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
@JsonPropertyOrder({"status", "page", "limit", "total", "data"})
@AllArgsConstructor
@NoArgsConstructor
public class GetProfilesDto {
    @JsonProperty("status")
    private String status;

    @JsonProperty("page")
    private int page;

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("total")
    private long total;

    @JsonProperty("data")
    private List<DataRepositoryDto> data;
}
