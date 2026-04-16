package com.hng.nameprocessing.services;

import com.fasterxml.uuid.Generators;
import com.hng.nameprocessing.dtos.*;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import com.hng.nameprocessing.repositories.DataRepository;
import com.hng.nameprocessing.services.restclients.AgifyClient;
import com.hng.nameprocessing.services.restclients.GenderizeClient;
import com.hng.nameprocessing.services.restclients.NationalizeClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class ProcessingService {
    private final AgifyClient agifyClient;
    private final GenderizeClient genderizeClient;
    private final NationalizeClient nationalizeClient;
    private final Executor executor;
    private final DataRepository dataRepository;

    public ProcessingService(AgifyClient agifyClient, GenderizeClient genderizeClient, NationalizeClient nationalizeClient, @Qualifier("asyncExecutor") Executor executor, DataRepository dataRepository) {
        this.agifyClient = agifyClient;
        this.genderizeClient = genderizeClient;
        this.nationalizeClient = nationalizeClient;
        this.executor = executor;
        this.dataRepository = dataRepository;
    }

    public CompletableFuture<ApiResponse> processName(String name) {
            CompletableFuture<AgifyResponse> agifyFuture = agifyClient.agifyRequest(name);
            CompletableFuture<NationalizeResponse> nationalizeFuture = nationalizeClient.nationalizeRequest(name);
            CompletableFuture<GenderizeResponse> genderizeFuture = genderizeClient.genderizeRequest(name);

            return CompletableFuture.allOf(agifyFuture, nationalizeFuture, genderizeFuture)
                    .thenApply(ignored -> aggregateResponse(agifyFuture.join(), nationalizeFuture.join(), genderizeFuture.join()));
    }

    private ApiResponse aggregateResponse(AgifyResponse agifyResponse,
                                               NationalizeResponse nationalizeResponse,
                                               GenderizeResponse genderizeResponse) {
        String gender = genderizeResponse.getGender();
        Double probability = genderizeResponse.getProbability();
        Long sampleSize = genderizeResponse.getCount();
        Integer age = agifyResponse.getAge();
        String ageGroup;
        List<Country> countryList = nationalizeResponse.getCountryList();

        if (gender == null || sampleSize == 0) {
            // throw error
            throw new ServiceValidationException("Genderize returned an invalid response", 502);
        }

        if (age == null) {
            // throw error
            throw new ServiceValidationException("Agify returned an invalid response", 502);
        }

        if (countryList.isEmpty()) {
            // throw error
            throw new ServiceValidationException("Nationalize returned an invalid response", 502);
        }

        // age classification
        if (age >= 0 && age <= 12) {
            ageGroup = "child";
        } else if (age >= 13 && age <= 19) {
            ageGroup = "teenager";
        } else if (age >= 20 && age <= 59) {
            ageGroup = "adult";
        } else if (age >= 60) {
            ageGroup = "senior";
        } else {
            throw new RuntimeException();
        }

        // country with max probability
        Country maxCountry = countryList.stream()
                .max(Comparator.comparingDouble(Country::getProbability))
                .orElseThrow(() -> new ServiceValidationException("Nationalize returned an invalid response", 502));

        BigDecimal countryProbability = new BigDecimal(maxCountry.getProbability()).setScale(2, RoundingMode.FLOOR);

        // building data dto
        DataDto dataDto = DataDto.builder()
                .id(Generators.timeBasedEpochGenerator().generate())
                .name(agifyResponse.getName())
                .gender(gender)
                .genderProbability(probability)
                .sampleSize(sampleSize)
                .age(agifyResponse.getAge())
                .ageGroup(ageGroup)
                .countryId(maxCountry.getCountryId())
                .countryProbability(countryProbability)
                .createdAt(Instant.now())
                .build();

        // transforming data dto to data mapping for persistence
        DataMapping dataMapping = dataDtoDatMapper(dataDto);

        // saving data mapping
        dataRepository.save(dataMapping);

        return FreshResponseDto.builder()
                .status("success")
                .data(dataDto)
                .build();
    }

    private DataMapping dataDtoDatMapper(DataDto dataDto){
       return new DataMapping(
               dataDto.getId(),
               dataDto.getName(),
               dataDto.getGender(),
               dataDto.getGenderProbability(),
               dataDto.getSampleSize(),
               dataDto.getAge(),
               dataDto.getAgeGroup(),
               dataDto.getCountryId(),
               dataDto.getCountryProbability(),
               dataDto.getCreatedAt()
        );
    }
}
