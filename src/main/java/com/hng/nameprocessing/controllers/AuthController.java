package com.hng.nameprocessing.controllers;

import com.hng.nameprocessing.dtos.authmapping.AuthResponse;
import com.hng.nameprocessing.dtos.authmapping.LoginRequest;
import com.hng.nameprocessing.dtos.authmapping.RefreshRequest;
import com.hng.nameprocessing.services.authservice.CLIAuth;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final CLIAuth cliAuth;

  public AuthController(CLIAuth cliAuth) {
    this.cliAuth = cliAuth;
  }

  @PostMapping("/token")
  public CompletableFuture<ResponseEntity<AuthResponse>> authenticate(
      @RequestBody LoginRequest request) {
    return cliAuth
        .authenticate(request.getCode(), request.getCodeVerifier())
        .thenApply(authResponse -> ResponseEntity.status(HttpStatus.OK).body(authResponse));
  }

  @PostMapping("/refresh")
  public CompletableFuture<ResponseEntity<AuthResponse>> refresh(
      @RequestBody RefreshRequest request) {
    return cliAuth
        .refresh(request.getRefreshToken())
        .thenApply(authResponse -> ResponseEntity.status(HttpStatus.OK).body(authResponse));
  }
}
