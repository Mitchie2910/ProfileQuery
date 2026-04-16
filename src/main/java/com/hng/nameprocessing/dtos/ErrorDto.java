package com.hng.nameprocessing.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@JsonPropertyOrder({"status", "message"})
@Data
@Builder
public class ErrorDto {
    private final String status = "error";
    private final String message;

    public ErrorDto(String message) {
        this.message = message;
    }
}
