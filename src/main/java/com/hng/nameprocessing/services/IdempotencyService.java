package com.hng.nameprocessing.services;

import com.hng.nameprocessing.dtos.*;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import com.hng.nameprocessing.repositories.CustomDataRepository;
import com.hng.nameprocessing.repositories.DataRepository;
import com.hng.nameprocessing.utility.NaturalLanguageProcessor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class IdempotencyService {

    private final ProcessingService processingService;
    private final DataRepository dataRepository;
    private final CustomDataRepository customDataRepository;
    private final Executor executor;
    private final NaturalLanguageProcessor naturalLanguageProcessor;


    public IdempotencyService(ProcessingService processingService, DataRepository dataRepository, @Qualifier("asyncExecutor")Executor executor, CustomDataRepository customDataRepository, NaturalLanguageProcessor naturalLanguageProcessor) {
        this.processingService = processingService;
        this.dataRepository = dataRepository;
        this.executor = executor;
        this.customDataRepository = customDataRepository;
        this.naturalLanguageProcessor = naturalLanguageProcessor;
    }

    public CompletableFuture<ApiResponse> processName(String name) {

        // Normalize input name
        String normalizedName = normalizeLowerCase(name);

        return CompletableFuture.supplyAsync(() ->
                dataRepository.findByName(normalizedName), executor)
                .thenCompose(optional -> optional
                         .map(dataMapping -> CompletableFuture.<ApiResponse>completedFuture(
                                 IdempotentResponseDto.builder()
                                         .status("success")
                                         .message("Profile already exists")
                                         .data(dataMapperDataDto(dataMapping))
                                         .build()
                         )

                 )
                         .orElseGet(() -> processingService.processName(normalizedName)));
    }

    public CompletableFuture<FreshResponseDto> getProfileById(UUID id){
        return CompletableFuture.supplyAsync(() ->
            dataRepository.findById(id), executor)
                .thenCompose(optional -> optional
                        .map(dataMapping -> CompletableFuture.completedFuture(
                                FreshResponseDto.builder()
                                        .status("success")
                                        .data(dataMapperDataDto(dataMapping))
                                        .build()
                                )
                        )
                        .orElseThrow(() -> new ServiceValidationException("Data not found for ID", 404)));

    }

    public CompletableFuture<GetProfilesDto> getProfiles(QueryParameters queryParameters, Integer pageLimit, Integer pageNumber, Sort sort) {

        final int safePageLimit = (pageLimit != null && pageLimit > 50)
                ? 50
                : pageLimit;
        return CompletableFuture.supplyAsync(() -> {

            // Normalize inputs
            String normalizedGender = normalizeLowerCase(queryParameters.getGender());
            String normalizedCountryId = normalizeUpperCase(queryParameters.getCountryId());
            String normalizedAgeGroup = normalizeLowerCase(queryParameters.getAgeGroup());

            List<DataRepositoryDto> resultList = new ArrayList<>();

            Specification<DataMapping> specs = buildSpecs(
                    normalizedGender,
                    normalizedCountryId,
                    normalizedAgeGroup,
                    queryParameters.getMinAge(),
                    queryParameters.getMaxAge(),
                    queryParameters.getMinGenderProbability(),
                    queryParameters.getMinCountryProbability());


            Pageable pageable = PageRequest.of(pageNumber-1, safePageLimit, sort);

            Page<DataMapping> page = dataRepository.findAll(specs, pageable);

            page.getContent()
                    .stream()
                    .map(this::mappingToDto)
                    .forEach(resultList::add);


            // Throw exception if result returns empty
            if (resultList.isEmpty()) {
                throw new ServiceValidationException("Data not found for query parameters", 404);
            }

            return GetProfilesDto.builder()
                    .status("success")
                    .page(pageNumber)
                    .limit(safePageLimit)
                    .total(page.getTotalElements())
                    .data(resultList)
                    .build();
        }, executor);

    }

    public CompletableFuture<Void> deleteProfile(UUID id){
        return CompletableFuture.runAsync(() -> {
            if(!dataRepository.existsById(id)){
                throw new ServiceValidationException("Profile does not exist", 404);
            }
            dataRepository.deleteById(id);

        }, executor);
    }

    public CompletableFuture<GetProfilesDto> intelligentSearch(String query, Integer pageLimit, Integer pageNumber, Sort sort) {
        return CompletableFuture.supplyAsync(() ->
                naturalLanguageProcessor.processQuery(query), executor)
                .thenCompose(queryParameters -> getProfiles(queryParameters, pageLimit, pageNumber, sort ));

    }

    // Build Query Specifications
    public Specification<DataMapping> buildSpecs(
            String gender,
            String countryId,
            String ageGroup,
            Integer minAge,
            Integer maxAge,
            Float minGenderProbability,
            Float minCountryProbability
    ) {
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

            if(minAge != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("age"), minAge));
            }

            if(maxAge != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("age"), maxAge));
            }

            if(minGenderProbability != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("genderProbability"), minGenderProbability));
            }

            if(minCountryProbability != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("countryProbability"), minCountryProbability));
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
    private String normalizeLowerCase(String text){
        return text == null ? null : text.replaceAll("\\s", "").toLowerCase();
    }

    // Helper method to normalize text to uppercase
    private String normalizeUpperCase(String text){
        return text == null ? null : text.replaceAll("\\s", "").toUpperCase();
    }

    // Helper method to normalize input name
    private String normalizeName(String name) {
        String noSpaceAndLowerCase = name.replaceAll("\\s", "").toLowerCase();
        return noSpaceAndLowerCase.substring(0,1).toUpperCase() + noSpaceAndLowerCase.substring(1);
    }

    // Helper method to map dataMapping to dataDto
    private DataDto dataMapperDataDto(DataMapping dataMapping){
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
                dataMapping.getCreatedAt()
        );
    }


}
