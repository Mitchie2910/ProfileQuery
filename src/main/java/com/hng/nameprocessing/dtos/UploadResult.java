package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UploadResult {

    @JsonProperty("success")
    private String status;
    @JsonProperty("total_rows")
    private int totalRows;
    @JsonProperty("inserted")
    private int inserted;
    @JsonProperty("skipped")
    private int skipped;
    @JsonProperty("reasons")
    private final Map<String, Integer> reasons = new HashMap<>();

    public void incrementReason(String reason) {
        reasons.merge(reason, 1, Integer::sum);
    }

}
