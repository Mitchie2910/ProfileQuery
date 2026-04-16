package com.hng.nameprocessing.RestClients;

import com.hng.nameprocessing.dtos.AgifyResponse;
import com.hng.nameprocessing.dtos.GenderizeResponse;
import com.hng.nameprocessing.services.restclients.AgifyClient;
import com.hng.nameprocessing.services.restclients.GenderizeClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class AgifyClientTest {
    @MockitoSpyBean
    AgifyClient agifyClient;

    @Test
    public void shouldReturnAgeDetails200(){
        // Given
        String name = "James";

        // When
        AgifyResponse response = agifyClient.agifyRequest(name).join();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(name);

        System.out.println(response.getName());
        System.out.println(response.getAge());
        System.out.println(response.getCount());
    }
}
