package com.hng.nameprocessing.ServiceTests;

import com.fasterxml.uuid.Generators;
import com.hng.nameprocessing.dtos.*;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import com.hng.nameprocessing.repositories.DataRepository;
import com.hng.nameprocessing.services.IdempotencyService;
import com.hng.nameprocessing.services.ProcessingService;
import com.hng.nameprocessing.services.restclients.AgifyClient;
import com.hng.nameprocessing.services.restclients.GenderizeClient;
import com.hng.nameprocessing.services.restclients.NationalizeClient;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
public class IdempotencyServiceTest {
    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private DataRepository dataRepository;

    @MockitoBean
    private AgifyClient agifyClient;

    @MockitoBean
    private GenderizeClient genderizeClient;

    @MockitoBean
    private NationalizeClient nationalizeClient;

    @BeforeEach
    void setUp() {
        GenderizeResponse genderizeResponse = new GenderizeResponse(
                5000L,
                "Edward",
                "male",
                0.92
        );

        NationalizeResponse nationalizeResponse = new NationalizeResponse(
                5000L,
                "Edward",
                List.of(new Country("NG", 0.54))
        );

        AgifyResponse agifyResponse = new AgifyResponse(
                5000L,
                "Edward",
                50
        );

        when(agifyClient.agifyRequest(anyString()))
                .thenReturn(CompletableFuture.completedFuture(agifyResponse));

        when(nationalizeClient.nationalizeRequest(anyString()))
                .thenReturn(CompletableFuture.completedFuture(nationalizeResponse));

        when(genderizeClient.genderizeRequest(anyString()))
                .thenReturn(CompletableFuture.completedFuture(genderizeResponse));
    }


    @Test
    public void shouldReturn200GetProfiles(){
        //Given
        QueryParameters parameters = QueryParameters.builder()
                .gender(null)
                .minGenderProbability(null)
                .maxAge(null)
                .minAge(null)
                .ageGroup(null)
                .minCountryProbability(null)
                .build();

        int pageNo = 1;
        int pageLimit = 10;
        Sort sort = Sort.by(Sort.DEFAULT_DIRECTION, "createdAt");

        //when
        GetProfilesDto profilesDto = idempotencyService.getProfiles(parameters, pageLimit, pageNo, sort).join();

        //Then
        assertThat(profilesDto).isNotNull();

        System.out.println(profilesDto);

    }

    @Test
    public void shouldReturn400ResultListEmptyGetProfiles(){
        //Given
        QueryParameters parameters = QueryParameters.builder()
                .gender("shemale")
                .minGenderProbability(null)
                .maxAge(null)
                .minAge(null)
                .ageGroup(null)
                .minCountryProbability(null)
                .build();

        int pageNo = 1;
        int pageLimit = 10;
        Sort sort = Sort.by(Sort.DEFAULT_DIRECTION, "createdAt");

        // when+then
        assertThatThrownBy(() -> idempotencyService.getProfiles(parameters, pageLimit, pageNo, sort).join())
                .cause()
                        .isInstanceOf(ServiceValidationException.class);



    }

    @Test
    public void shouldReturn200SearchProfiles(){
        //Given

        String query = "male and female above 30 and has gender confidence above 0.6";

        int pageNo = 1;
        int pageLimit = 10;
        Sort sort = Sort.by(Sort.DEFAULT_DIRECTION, "createdAt");

        //when
        GetProfilesDto profilesDto = idempotencyService.intelligentSearch(query, pageLimit, pageNo, sort).join();

        //Then
        assertThat(profilesDto).isNotNull();
    }

    @Test
    public void shouldReturn404SearchProfiles(){
        //Given

        String query = "male and female above 30 and is a teenager";

        int pageNo = 1;
        int pageLimit = 10;
        Sort sort = Sort.by(Sort.DEFAULT_DIRECTION, "createdAt");

        //when+then
        assertThatThrownBy(() -> idempotencyService.intelligentSearch(query, pageLimit, pageNo, sort).join())
                .cause()
                        .isInstanceOf(ServiceValidationException.class);
    }

    @Test
    public void shouldReturnSortedProfiles(){
        //Given
        QueryParameters parameters = QueryParameters.builder()
                .gender(null)
                .minGenderProbability(null)
                .maxAge(null)
                .minAge(null)
                .ageGroup(null)
                .minCountryProbability(null)
                .build();

        int pageNo = 1;
        int pageLimit = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        //when
        GetProfilesDto profilesDto = idempotencyService.getProfiles(parameters, pageLimit, pageNo, sort).join();

        //Then
        assertThat(profilesDto).isNotNull();

        System.out.println(profilesDto);
    }
}
