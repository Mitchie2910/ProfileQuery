package com.hng.nameprocessing.RestClients;

import com.hng.nameprocessing.dtos.GenderizeResponse;
import com.hng.nameprocessing.services.restclients.GenderizeClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class GenderizeClientTest {
    @MockitoSpyBean
    GenderizeClient genderizeClient;

    @Test
    public void shouldReturnNameDetails200(){
        // Given
        String name = "James";

        // When
        GenderizeResponse response = genderizeClient.genderizeRequest(name).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getGender()).isEqualTo("male");
    }
}
