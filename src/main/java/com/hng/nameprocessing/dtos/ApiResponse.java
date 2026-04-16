package com.hng.nameprocessing.dtos;

import lombok.Data;

public interface ApiResponse {
    DataDto getData();

    String getStatus();
}
