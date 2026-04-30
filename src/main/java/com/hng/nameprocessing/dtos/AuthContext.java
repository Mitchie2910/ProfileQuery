package com.hng.nameprocessing.dtos;


public record AuthContext(
        String userId,
        Role role
) {
}
