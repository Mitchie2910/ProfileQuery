package com.hng.nameprocessing.repositories;

import com.hng.nameprocessing.dtos.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  User findByGithubId(String githubId);
}
