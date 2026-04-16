package com.hng.nameprocessing.controllers;

import com.hng.nameprocessing.dtos.ApiResponse;
import com.hng.nameprocessing.dtos.FreshResponseDto;
import com.hng.nameprocessing.dtos.GetProfilesDto;
import com.hng.nameprocessing.dtos.RequestModel;
import com.hng.nameprocessing.services.IdempotencyService;
import com.hng.nameprocessing.validation.ValidationOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Validated

public class NameProcessingController {

    private final IdempotencyService idempotencyService;

    public NameProcessingController(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    @PostMapping("/profiles")
    public CompletableFuture<ResponseEntity<ApiResponse>> processName(
            @Validated(ValidationOrder.class) @RequestBody
            RequestModel requestModel
    ){
        return idempotencyService.processName(requestModel.getName())
                .thenApply( apiResponse -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(apiResponse)
                );
    }

    @GetMapping("/profiles")
    public CompletableFuture<ResponseEntity<GetProfilesDto>> getProfiles(
            @RequestParam(value = "gender", required = false)
            @Pattern(regexp = "^[a-zA-Z]+$", message = "name must contain only letters")
            String gender,

            @RequestParam(value = "country_id", required = false)
            @Pattern(regexp = "^[a-zA-Z]+$", message = "name must contain only letters")
            String countryId,

            @RequestParam(value = "age_group", required = false)
            @Pattern(regexp = "^[a-zA-Z]+$", message = "name must contain only letters")
            String ageGroup
    ) {
        return idempotencyService.getProfiles(gender, countryId, ageGroup)
                .thenApply(getProfilesDto -> ResponseEntity
                        .status(HttpStatus.OK)
                        .body(getProfilesDto));
    }

    @GetMapping("/profiles/{id}")
    public CompletableFuture<ResponseEntity<FreshResponseDto>> getProfileById(@PathVariable UUID id) {
        return idempotencyService.getProfileById(id)
                .thenApply(freshResponseDto -> ResponseEntity
                        .status(HttpStatus.OK)
                        .body(freshResponseDto));
    }

    @DeleteMapping("/profiles/{id}")
    public CompletableFuture<ResponseEntity<?>> deleteProfile(@PathVariable UUID id) {
        return idempotencyService.deleteProfile(id)
                .thenApply(v -> ResponseEntity.noContent().build());
    }
}
