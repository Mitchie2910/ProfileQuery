package com.hng.nameprocessing.repositories;

import com.hng.nameprocessing.dtos.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>{

    User findByGithubId(String githubId);

}
