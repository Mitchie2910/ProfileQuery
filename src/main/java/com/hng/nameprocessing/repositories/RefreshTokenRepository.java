package com.hng.nameprocessing.repositories;

import com.hng.nameprocessing.dtos.RefreshTokenMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenMapping, String> {
    RefreshTokenMapping findByTokenId(String tokenId);
}
