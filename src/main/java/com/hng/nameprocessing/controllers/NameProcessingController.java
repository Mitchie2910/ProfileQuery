package com.hng.nameprocessing.controllers;

import com.hng.nameprocessing.configuration.CustomPrincipal;
import com.hng.nameprocessing.dtos.*;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import com.hng.nameprocessing.services.IdempotencyService;
import com.hng.nameprocessing.services.UploadService;
import com.hng.nameprocessing.validation.ValidationOrder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api")
@Validated
public class NameProcessingController {

  private final IdempotencyService idempotencyService;
  private final Executor executor;
  private final UploadService uploadService;

  public NameProcessingController(
      IdempotencyService idempotencyService, @Qualifier("asyncExecutor") Executor executor, UploadService uploadService) {
    this.idempotencyService = idempotencyService;
    this.executor = executor;
    this.uploadService = uploadService;
  }

  @PostMapping("/profiles")
  public CompletableFuture<ResponseEntity<ApiResponse>> processName(
      @Validated(ValidationOrder.class) @RequestBody RequestModel requestModel) {
    return idempotencyService
        .processName(requestModel.getName())
        .thenApply(
            apiResponse -> {
              if (apiResponse instanceof FreshResponseDto) {
                return ResponseEntity.status(201).body(apiResponse);
              } else {
                return ResponseEntity.status(200).body(apiResponse);
              }
            });
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
      @RequestParam(value = "min_age", required = false) Integer minAge,
      @RequestParam(value = "max_age", required = false) Integer maxAge,
      @RequestParam(value = "min_gender_probability", required = false) Float minGenderProbability,
      @RequestParam(value = "min_country_probability", required = false) Float minCountryProbability,
      @RequestParam(value = "sort_by", required = false) String sortBy,
      @RequestParam(value = "order", required = false) String sortOrder,
      @RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
      @RequestParam(value = "limit", required = false, defaultValue = "10") Integer pageLimit,
      HttpServletRequest request) {

    QueryParameters parameters =
        QueryParameters.builder()
            .gender(gender)
            .countryId(countryId)
            .ageGroup(ageGroup)
            .minAge(minAge)
            .maxAge(maxAge)
            .minGenderProbability(minGenderProbability)
            .minCountryProbability(minCountryProbability)
            .build();

      String validatedSortBy = validateSortBy(sortBy);
      String validatedOrder = validateOrder(sortOrder);

    return idempotencyService
        .getProfiles(parameters, pageLimit, pageNumber, validatedSortBy, validatedOrder)
        .thenApply(getProfilesDto -> ResponseEntity.status(HttpStatus.OK).body(getProfilesDto));
  }

  @GetMapping("/profiles/search")
  public CompletableFuture<ResponseEntity<GetProfilesDto>> getProfileBySearch(
      @RequestParam(value = "q") @NotBlank(message = "Search query cannot be blank") String query,
      @RequestParam(value = "sort_by", required = false) String sortBy,
      @RequestParam(value = "order", required = false) String sortOrder,
      @RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
      @RequestParam(value = "limit", required = false, defaultValue = "10") Integer pageLimit) {
    String validatedSortBy = validateSortBy(sortBy);
    String validatedOrder = validateOrder(sortOrder);

    return idempotencyService
        .intelligentSearch(query, pageLimit, pageNumber, validatedSortBy, validatedOrder)
        .thenApply(getProfilesDto -> ResponseEntity.status(HttpStatus.OK).body(getProfilesDto));
  }

  @GetMapping("/profiles/{id}")
  public CompletableFuture<ResponseEntity<FreshResponseDto>> getProfileById(@PathVariable UUID id) {
    return idempotencyService
        .getProfileById(id)
        .thenApply(freshResponseDto -> ResponseEntity.status(HttpStatus.OK).body(freshResponseDto));
  }

  @DeleteMapping("/profiles/{id}")
  public CompletableFuture<ResponseEntity<?>> deleteProfile(@PathVariable UUID id) {
    return idempotencyService.deleteProfile(id).thenApply(v -> ResponseEntity.noContent().build());
  }

  @GetMapping(value = "/profiles/export", produces = "text/csv")
  public ResponseEntity<StreamingResponseBody> exportProfiles(
      @RequestParam(name = "format", defaultValue = "csv") String format,
      @RequestParam(value = "gender", required = false)
          @Pattern(regexp = "^[a-zA-Z]+$", message = "Invalid query parameters")
          String gender,
      @RequestParam(value = "country_id", required = false)
          @Pattern(regexp = "^[a-zA-Z]+$", message = "Invalid query parameters")
          String countryId,
      @RequestParam(value = "age_group", required = false)
          @Pattern(regexp = "^[a-zA-Z]+$", message = "Invalid query parameters")
          String ageGroup,
      @RequestParam(value = "min_age", required = false) Integer minAge,
      @RequestParam(value = "max_age", required = false) Integer maxAge,
      @RequestParam(value = "min_gender_probability", required = false) Float minGenderProbability,
      @RequestParam(value = "min_country_probability", required = false)
          Float minCountryProbability,
      @RequestParam(value = "sort_by", required = false) String sortBy,
      @RequestParam(value = "order", required = false) String sortOrder,
      @RequestParam(value = "page", required = false, defaultValue = "1") Integer pageNumber,
      @RequestParam(value = "limit", required = false, defaultValue = "10") Integer pageLimit,
      HttpServletResponse response)
      throws IOException {

    QueryParameters parameters =
        QueryParameters.builder()
            .gender(gender)
            .countryId(countryId)
            .ageGroup(ageGroup)
            .minAge(minAge)
            .maxAge(maxAge)
            .minGenderProbability(minGenderProbability)
            .minCountryProbability(minCountryProbability)
            .build();

    String validatedSortBy = validateSortBy(sortBy);
    String validatedOrder= validateOrder(sortOrder);


    try {
      StreamingResponseBody body =
          outputStream -> {
            idempotencyService.exportProfileCsv(parameters, outputStream, validatedSortBy, validatedOrder);
          };

      return ResponseEntity.ok()
          .header("Content-Disposition", "attachment; filename=data.csv")
          .contentType(MediaType.TEXT_PLAIN)
          .body(body);
    } catch (Exception e) {
      throw new ServiceValidationException("Error exporting", 500);
    }
  }

  @GetMapping("/me")
  public ResponseEntity<UserDto> me(@AuthenticationPrincipal CustomPrincipal principal) {
    return ResponseEntity.ok().body(principal.getUser());
  }

  @PostMapping("/upload")
  public CompletableFuture< ResponseEntity<UploadResult>> upload(@RequestParam("file") MultipartFile file)
          throws IOException {

    return uploadService.handleUpload(file)
            .thenApply(uploadResult -> ResponseEntity.accepted()
                    .body(uploadResult));
  }

  private String validateSortBy(String sortBy) {
    if (sortBy == null || sortBy.isBlank()) {
      return "createdAt"; // default fallback
    }

    sortBy = sortBy.trim();

    Map<String, String> allowedFields =
        Map.of(
            "age", "age",
            "created_at", "createdAt",
            "gender_probability", "genderProbability");

    if (!allowedFields.containsKey(sortBy)) {
      throw new ServiceValidationException("Invalid entry for sort_by parameter", 422);
    }

    return allowedFields.get(sortBy);
  }

  private String validateOrder(String order) {
    if (order == null || order.isBlank()) {
      return "asc"; // default fallback
    }

    order = order.trim();

    Set<String> allowedFields = Set.of("asc", "desc");

    if (!allowedFields.contains(order)) {
      throw new ServiceValidationException("Invalid entry for order parameter", 422);
    }

    return order;
  }

  private Sort buildSort(String sortBy, Sort.Direction sortDirection) {
    return Sort.by(sortDirection, sortBy);
  }
}
