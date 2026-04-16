package com.hng.nameprocessing.ServiceTests;

import com.fasterxml.uuid.Generators;
import com.hng.nameprocessing.dtos.*;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import com.hng.nameprocessing.services.IdempotencyService;
import com.hng.nameprocessing.services.ProcessingService;
import com.hng.nameprocessing.services.restclients.AgifyClient;
import com.hng.nameprocessing.services.restclients.GenderizeClient;
import com.hng.nameprocessing.services.restclients.NationalizeClient;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

//    @Test
//    public void shouldReturnApiDataDto200(){
//        // given
//        String name = "John";
//        String name2 = "john";
//
//        // when
//        ApiResponse freshResponseDto1 = idempotencyService.processName(name).join();
//        ApiResponse freshResponseDto2 = idempotencyService.processName(name2).join();
//
//        // then
//        assertThat(freshResponseDto1).isInstanceOf(FreshResponseDto.class);
//        assertThat(freshResponseDto2).isInstanceOf(IdempotentResponseDto.class);
//        assertThat(freshResponseDto2.getStatus()).isEqualTo("success");
//        assertThat(freshResponseDto2.getData().getId()).isEqualTo(freshResponseDto1.getData().getId());
//
//    }
//
//    @Test
//    public void shouldReturnApiDataDtoById(){
//        // given
//        String name = "Jane";
//        ApiResponse freshResponseDto1 = idempotencyService.processName(name).join();
//        UUID id = freshResponseDto1.getData().getId();
//
//        // when
//        FreshResponseDto responseDto = idempotencyService.getProfileById(id).join();
//
//        // then
//        assertThat(freshResponseDto1.getData()).isEqualTo(responseDto.getData());
//
//    }

    @Test
    public void shouldThrowServiceValidationException404() {
        // given
        String name = "Edward";



        ApiResponse freshResponseDto1 = idempotencyService.processName(name).join();

        UUID id = Generators.timeBasedEpochGenerator().generate();


        // when + then
        assertThatThrownBy(()->idempotencyService.getProfileById(id).join())
                .cause()
                .hasMessage("Data not found for ID");

    }

    @Test
    public void shouldReturnFilteredProfiles200() {

        // given
        String name = "Edward";

        ApiResponse freshResponseDto1 = idempotencyService.processName(name).join();

        String gender = null;
        String countryId = null;
        String ageGroup = null;

        GetProfilesDto result = idempotencyService.getProfiles(gender, countryId, ageGroup).join();

        assertThat(result).isNotNull();
        assertThat(result.getData().get(0).getAge()).isEqualTo(freshResponseDto1.getData().getAge());

    }

    @Test
    public void shouldThrowServiceValidationExceptionDataNotFound404() {
        String name = "Edward";

        ApiResponse freshResponseDto1 = idempotencyService.processName(name).join();

        String gender = "male";
        String countryId = "NG";
        String ageGroup = "child";

        assertThatThrownBy(() -> idempotencyService.getProfiles(gender, countryId, ageGroup).join())
                .cause()
                .isInstanceOf(ServiceValidationException.class)
                .hasMessage("Data not found for query parameters");

    }
}
