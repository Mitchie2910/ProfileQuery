package com.hng.nameprocessing.ServiceTests;

import com.hng.nameprocessing.dtos.ApiResponse;
import com.hng.nameprocessing.dtos.FreshResponseDto;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import com.hng.nameprocessing.services.ProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class ProcessingServiceTest {

    @Autowired
    private ProcessingService processingService;

    @Test
    public void shouldReturnApiDataDto200(){
        // given
        String name = "John";

        // when
        ApiResponse freshResponseDto = processingService.processName(name).join();

        // then
        assertThat(freshResponseDto).isNotNull();
        assertThat(freshResponseDto.getData()).isNotNull();
        assertThat(freshResponseDto.getData().getGender()).isEqualTo("male");
        assertThat(freshResponseDto.getStatus()).isEqualTo("success");
        System.out.println(freshResponseDto.getData().getCountryId());
        System.out.println(freshResponseDto.getData().getCountryProbability());

    }

    @Test
    public void shouldReturnValidNameInvalidAge400(){
        // given
        String name = "ponzing";

        // when + then
        assertThatThrownBy(() -> {processingService.processName(name).join();})
                .cause()
                .isInstanceOf(ServiceValidationException.class)
                .hasMessage("Age not available");
    }
}
