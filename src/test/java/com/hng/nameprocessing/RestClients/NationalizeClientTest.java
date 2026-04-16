package com.hng.nameprocessing.RestClients;

import com.hng.nameprocessing.dtos.AgifyResponse;
import com.hng.nameprocessing.dtos.NationalizeResponse;
import com.hng.nameprocessing.services.restclients.AgifyClient;
import com.hng.nameprocessing.services.restclients.NationalizeClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class NationalizeClientTest {
    @MockitoSpyBean
    NationalizeClient nationalizeClient;

    @Test
    public void shouldReturnNationDetails200(){
        // Given
        String name = "James";

        // When
        NationalizeResponse response = nationalizeClient.nationalizeRequest(name).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(name);
    }
}
