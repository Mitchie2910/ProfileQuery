package com.hng.nameprocessing.services;

import com.hng.nameprocessing.dtos.*;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import com.hng.nameprocessing.repositories.DataRepository;
import com.hng.nameprocessing.repositories.StreamRepositoryImpl;
import com.hng.nameprocessing.utility.NaturalLanguageProcessor;
import jakarta.persistence.criteria.Predicate;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

  private final ProcessingService processingService;
  private final DataRepository dataRepository;
  private final StreamRepositoryImpl streamRepositoryImpl;
  private final Executor executor;
  private final NaturalLanguageProcessor naturalLanguageProcessor;

  Logger logger = LoggerFactory.getLogger(IdempotencyService.class);

  public IdempotencyService(
      ProcessingService processingService,
      DataRepository dataRepository,
      @Qualifier("asyncExecutor") Executor executor,
      StreamRepositoryImpl streamRepositoryImpl,
      NaturalLanguageProcessor naturalLanguageProcessor) {
    this.processingService = processingService;
    this.dataRepository = dataRepository;
    this.executor = executor;
    this.streamRepositoryImpl = streamRepositoryImpl;
    this.naturalLanguageProcessor = naturalLanguageProcessor;
  }

  @Cacheable("profile")
  public CompletableFuture<ApiResponse> processName(String name) {

    // Normalize input name
    String normalizedName = normalizeLowerCase(name);

    return CompletableFuture.supplyAsync(() -> dataRepository.findByName(normalizedName), executor)
        .thenCompose(
            optional ->
                optional
                    .map(
                        dataMapping ->
                            CompletableFuture.<ApiResponse>completedFuture(
                                IdempotentResponseDto.builder()
                                    .status("success")
                                    .message("Profile already exists")
                                    .data(dataMapperDataDto(dataMapping))
                                    .build()))
                    .orElseGet(() -> processingService.processName(normalizedName)));
  }

  @Cacheable(value = "id")
  public CompletableFuture<FreshResponseDto> getProfileById(UUID id) {
    return CompletableFuture.supplyAsync(() -> dataRepository.findById(id), executor)
        .thenCompose(
            optional ->
                optional
                    .map(
                        dataMapping ->
                            CompletableFuture.completedFuture(
                                FreshResponseDto.builder()
                                    .status("success")
                                    .data(dataMapperDataDto(dataMapping))
                                    .build()))
                    .orElseThrow(
                        () -> new ServiceValidationException("Data not found for ID", 404)));
  }

  @Cacheable(value = "results",
            key = "#queryParameters.toKey() + ':' + #pageLimit + ':' + #pageNumber + ':' + #sortBy + ':' + #sortOrder"
  )
  public CompletableFuture<GetProfilesDto> getProfiles(
      QueryParameters queryParameters, Integer pageLimit, Integer pageNumber, String sortBy, String sortOrder) {

    final int safePageLimit = (pageLimit != null && pageLimit > 50) ? 50 : pageLimit;
    return CompletableFuture.supplyAsync(
        () -> {

          // Normalize inputs
          String normalizedGender = normalizeLowerCase(queryParameters.getGender());
          String normalizedCountryId = normalizeUpperCase(queryParameters.getCountryId());
          String normalizedAgeGroup = normalizeLowerCase(queryParameters.getAgeGroup());

          List<DataRepositoryDto> resultList = new ArrayList<>();

          Specification<DataMapping> specs =
              buildSpecs(
                  normalizedGender,
                  normalizedCountryId,
                  normalizedAgeGroup,
                  queryParameters.getMinAge(),
                  queryParameters.getMaxAge(),
                  queryParameters.getMinGenderProbability(),
                  queryParameters.getMinCountryProbability());

          Sort sort = Sort.by(Sort.Direction.valueOf(sortOrder), sortBy);

          Pageable pageable = PageRequest.of(pageNumber - 1, safePageLimit, sort);

          Page<DataMapping> page = dataRepository.findAll(specs, pageable);

          page.getContent().stream().map(this::mappingToDto).forEach(resultList::add);

          // Throw exception if result returns empty
          if (resultList.isEmpty()) {
            throw new ServiceValidationException("Data not found for query parameters", 404);
          }
          String baseUrl = "/api/profiles";

          String next =
              page.hasNext()
                  ? baseUrl + "?page=" + (pageNumber + 1) + "&limit=" + safePageLimit
                  : null;

          String prev =
              page.hasPrevious()
                  ? baseUrl + "?page=" + (pageNumber - 1) + "&limit=" + safePageLimit
                  : null;

          Links links =
              Links.builder()
                  .self("/api/profiles?page=%d&limit=%d".formatted(pageNumber, safePageLimit))
                  .next(next)
                  .prev(prev)
                  .build();

          return GetProfilesDto.builder()
              .status("success")
              .page(pageNumber)
              .limit(safePageLimit)
              .total(page.getTotalElements())
              .totalPages(page.getTotalPages())
              .links(links)
              .data(resultList)
              .build();
        },
        executor);
  }

  @Transactional
  public void exportProfileCsv(
      QueryParameters queryParameters, OutputStream outputStream, String sortBy, String sortOrder) throws IOException {

    Sort sort = Sort.by(Sort.Direction.valueOf(sortOrder), sortBy);

    Specification<DataMapping> spec =
        buildSpecs(
            queryParameters.getGender(),
            queryParameters.getCountryId(),
            queryParameters.getAgeGroup(),
            queryParameters.getMinAge(),
            queryParameters.getMaxAge(),
            queryParameters.getMinGenderProbability(),
            queryParameters.getMinCountryProbability());

    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

    writer.write(
        "id,name,gender,gender_probability,age,age_group,country_id,country_name,country_probability,created_at");
    writer.newLine();

    int page = 0;
    while (true) {

      Page<DataMapping> batch = dataRepository.findAll(spec, PageRequest.of(page, 500, sort));

      if (!batch.hasContent()) break;

      for (DataMapping d : batch) {
        writer.write(formatCsv(d));
        writer.newLine();
      }

      writer.flush();

      page++;
    }

  }

  @CacheEvict(value = "results", key = "#id")
  public CompletableFuture<Void> deleteProfile(UUID id) {
    return CompletableFuture.runAsync(
        () -> {
          if (!dataRepository.existsById(id)) {
            throw new ServiceValidationException("Profile does not exist", 404);
          }
          dataRepository.deleteById(id);
        },
        executor);
  }

  @Cacheable(value = "results",
          key = "#queryParameters.toKey() + ':' + #pageLimit + ':' + #pageNumber + ':' + #sortBy + ':' + #sortOrder"
  )
  public CompletableFuture<GetProfilesDto> intelligentSearch(
      String query, Integer pageLimit, Integer pageNumber, String sortBy, String sortOrder){
    return CompletableFuture.supplyAsync(
            () -> naturalLanguageProcessor.processQuery(query), executor)
        .thenCompose(queryParameters -> getProfiles(queryParameters, pageLimit, pageNumber, sortBy, sortOrder));
  }

  // Build Query Specifications
  public Specification<DataMapping> buildSpecs(
      String gender,
      String countryId,
      String ageGroup,
      Integer minAge,
      Integer maxAge,
      Float minGenderProbability,
      Float minCountryProbability) {
    return ((root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (gender != null) {
        predicates.add(criteriaBuilder.equal(root.get("gender"), gender));
      }

      if (countryId != null) {
        predicates.add(criteriaBuilder.equal(root.get("countryId"), countryId));
      }

      if (ageGroup != null) {
        predicates.add(criteriaBuilder.equal(root.get("ageGroup"), ageGroup));
      }

      if (minAge != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("age"), minAge));
      }

      if (maxAge != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("age"), maxAge));
      }

      if (minGenderProbability != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(
                root.get("genderProbability"), minGenderProbability));
      }

      if (minCountryProbability != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(
                root.get("countryProbability"), minCountryProbability));
      }

      return criteriaBuilder.and(predicates);
    });
  }

  // Helper to map data mapping to data repository dto
  DataRepositoryDto mappingToDto(DataMapping mapping) {
    return DataRepositoryDto.builder()
        .id(mapping.getId())
        .name(mapping.getName().toLowerCase())
        .age(mapping.getAge())
        .gender(mapping.getGender())
        .ageGroup(mapping.getAgeGroup())
        .countryName(mapping.getCountryName())
        .countryProbability(mapping.getCountryProbability())
        .createdAt(mapping.getCreatedAt())
        .countryId(mapping.getCountryId())
        .genderProbability(mapping.getGenderProbability())
        .build();
  }

  // Helper method to normalize text to lowercase
  private String normalizeLowerCase(String text) {
    return text == null ? null : text.replaceAll("\\s", "").toLowerCase();
  }

  // Helper method to normalize text to uppercase
  private String normalizeUpperCase(String text) {
    return text == null ? null : text.replaceAll("\\s", "").toUpperCase();
  }

  // Helper method to normalize input name
  private String normalizeName(String name) {
    String noSpaceAndLowerCase = name.replaceAll("\\s", "").toLowerCase();
    return noSpaceAndLowerCase.substring(0, 1).toUpperCase() + noSpaceAndLowerCase.substring(1);
  }

  // Helper method to map dataMapping to dataDto
  private DataDto dataMapperDataDto(DataMapping dataMapping) {
    return new DataDto(
        dataMapping.getId(),
        dataMapping.getName().toLowerCase(),
        dataMapping.getGender(),
        dataMapping.getGenderProbability(),
        dataMapping.getAge(),
        dataMapping.getAgeGroup(),
        dataMapping.getCountryId(),
        dataMapping.getCountryName(),
        dataMapping.getCountryProbability(),
        dataMapping.getCreatedAt());
  }

  private String safe(Object value) {
    if (value == null) return "";

    String str = value.toString();

    // escape quotes
    str = str.replace("\"", "\"\"");

    // wrap in quotes if needed (comma, newline, quote)
    if (str.contains(",") || str.contains("\n") || str.contains("\"")) {
      str = "\"" + str + "\"";
    }
    return str;
  }

  private String formatCsv(DataMapping d) {
    return String.join(
        ",",
        safe(d.getId()),
        safe(d.getName()),
        safe(d.getGender()),
        String.valueOf(d.getGenderProbability()),
        String.valueOf(d.getAge()),
        safe(d.getAgeGroup()),
        safe(d.getCountryId()),
        safe(d.getCountryName()),
        String.valueOf(d.getCountryProbability()),
        String.valueOf(d.getCreatedAt()));
  }
}
