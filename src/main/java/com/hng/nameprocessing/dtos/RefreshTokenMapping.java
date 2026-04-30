package com.hng.nameprocessing.dtos;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "refresh_table", schema = "mapping")


public class RefreshTokenMapping {
    @Id
    private String tokenId;
    private String githubId;
    private boolean revoked;
    private Instant expiresAt;

}
