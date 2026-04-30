package com.hng.nameprocessing.services;

import com.fasterxml.uuid.Generators;
import com.hng.nameprocessing.dtos.Role;
import com.hng.nameprocessing.dtos.User;
import com.hng.nameprocessing.dtos.authmapping.GitHubUser;
import com.hng.nameprocessing.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findOrCreateUser(String githubId, String username, String email, String avaterUrl){
        User user = userRepository.findByGithubId(githubId);

        if (user == null){
            user = new User();
            user.setId(Generators.timeBasedEpochGenerator().generate());
            user.setGithubId(githubId);
            user.setUsername(username);
            user.setEmail(email);
            user.setAvatarUrl(avaterUrl);
            if (userRepository.findAll().isEmpty()){
                user.setRole(Role.ROLE_ADMIN);
            }
            else {
                user.setRole(Role.ROLE_ANALYST);
            }
            user.setActive(true);
            user.setLastLoginAt(Instant.now());
            user.setCreatedAt(Instant.now());
            userRepository.save(user);
        }
        else {
            user.setActive(true);
            user.setLastLoginAt(Instant.now());
        }
        userRepository.save(user);
        return user;
    }

    public User findOrCreateUser(GitHubUser gitHubUser){
        User user = userRepository.findByGithubId(gitHubUser.getId().toString());
        if (user == null){
            user = new User();
            user.setId(Generators.timeBasedEpochGenerator().generate());
            user.setGithubId(gitHubUser.getId().toString());
            user.setUsername(gitHubUser.getLogin());
            user.setEmail(gitHubUser.getEmail());
            user.setAvatarUrl(gitHubUser.getAvatarUrl());
            if (gitHubUser.getEmail() != null && gitHubUser.getEmail().equals("mitchelntuen@gmail.com")){
                user.setRole(Role.ROLE_ADMIN);
            }
            else {
                user.setRole(Role.ROLE_ANALYST);
            }
            user.setActive(true);
            user.setLastLoginAt(Instant.now());
            user.setCreatedAt(Instant.now());
            userRepository.save(user);
        }
        else {
            user.setActive(true);
            user.setLastLoginAt(Instant.now());
        }
        userRepository.save(user);
        return user;
    }

    public User findUser(String githubId){
        return userRepository.findByGithubId(githubId);
    }

}
