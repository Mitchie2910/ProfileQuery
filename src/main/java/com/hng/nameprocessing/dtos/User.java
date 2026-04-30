package com.hng.nameprocessing.dtos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
@Table(name = "app_user", schema = "mapping")

public class User {
    @Id
    private UUID id;
    private String githubId;
    private String username;
    private String email;
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean isActive;
    private Instant lastLoginAt;
    private Instant createdAt;


}
