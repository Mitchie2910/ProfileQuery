package com.hng.nameprocessing.controllers;

import com.hng.nameprocessing.dtos.*;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import com.hng.nameprocessing.services.IdempotencyService;
import com.hng.nameprocessing.validation.ValidationOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
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
                .thenApply( apiResponse -> {
                    if (apiResponse instanceof FreshResponseDto){
                        return ResponseEntity
                                .status(201)
                                .body(apiResponse);
                    }
                    else {
                        return ResponseEntity
                                .status(200)
                                .body(apiResponse);
                    }
                        }
                );
    }

    @GetMapping("/profiles")
    public CompletableFuture<ResponseEntity<GetProfilesDto>> getProfiles(
            @RequestParam(value = "gender", required = false)
            @Pattern(regexp = "^[a-zA-Z]+$", message = "Invalid query parameters")
            String gender,

            @RequestParam(value = "country_id", required = false)
            @Pattern(regexp = "^[a-zA-Z]+$", message = "Invalid query parameters")
            String countryId,

            @RequestParam(value = "age_group", required = false)
            @Pattern(regexp = "^[a-zA-Z]+$", message = "Invalid query parameters")
            String ageGroup,

            @RequestParam(value = "min_age", required = false)
            Integer minAge,

            @RequestParam(value = "max_age", required = false)
            Integer maxAge,

            @RequestParam(value = "min_gender_probability", required = false)
            Float minGenderProbability,

            @RequestParam(value = "min_country_probability", required = false)
            Float minCountryProbability,

            @RequestParam(value = "sort_by", required = false)
            String sortBy,

            @RequestParam(value = "order", required = false)
            String sortOrder,

            @RequestParam(value = "page", required = false, defaultValue = "1")
            Integer pageNumber,

            @RequestParam(value = "limit", required = false, defaultValue = "10")
            Integer pageLimit

    ) {
        QueryParameters parameters = QueryParameters.builder()
                .gender(gender)
                .countryId(countryId)
                .ageGroup(ageGroup)
                .minAge(minAge)
                .maxAge(maxAge)
                .minGenderProbability(minGenderProbability)
                .minCountryProbability(minCountryProbability)
                .build();

        String validatedSortBy = validateSortBy(sortBy);
        Sort.Direction sortDirection = validateOrder(sortOrder);
        Sort buildSort = buildSort(validatedSortBy, sortDirection);

        return idempotencyService.getProfiles(parameters, pageLimit, pageNumber, buildSort)
                .thenApply(getProfilesDto -> ResponseEntity
                        .status(HttpStatus.OK)
                        .body(getProfilesDto));
    }

    @GetMapping("/profiles/search")
    public  CompletableFuture<ResponseEntity<GetProfilesDto>> getProfileBySearch(
            @RequestParam(value = "q") @NotBlank(message = "Search query cannot be blank") String query,
            @RequestParam(value = "sort_by", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String sortOrder,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer pageLimit
    ) {
        String validatedSortBy = validateSortBy(sortBy);
        Sort.Direction sortDirection = validateOrder(sortOrder);
        Sort buildSort = buildSort(validatedSortBy, sortDirection);
        return idempotencyService.intelligentSearch(query, pageLimit, pageNumber, buildSort)
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

    private String validateSortBy(String sortBy){
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt"; // default fallback
        }

        sortBy = sortBy.trim();

        Map<String, String> allowedFields = Map.of(
                "age", "age",
                "created_at", "createdAt",
                "gender_probability", "genderProbability"
        );

        if (!allowedFields.containsKey(sortBy)) {
            throw new ServiceValidationException("Invalid entry for sort_by parameter", 422);
        }

        return allowedFields.get(sortBy);
    }

    private Sort.Direction validateOrder(String order){
        if (order == null || order.isBlank()) {
            return Sort.DEFAULT_DIRECTION; // default fallback
        }

        order = order.trim();

        Set<String> allowedFields = Set.of(
                "asc",
                "desc"
        );

        if (!allowedFields.contains(order)) {
            throw new ServiceValidationException("Invalid entry for order parameter", 422);
        }

        return Sort.Direction.fromString(order);
    }

    private Sort buildSort(String sortBy, Sort.Direction sortDirection) {
        return Sort.by(sortDirection, sortBy);
    }
}
