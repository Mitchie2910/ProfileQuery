package com.hng.nameprocessing.ControllerTests;

import com.hng.nameprocessing.controllers.NameProcessingController;
import com.hng.nameprocessing.dtos.*;
import com.hng.nameprocessing.services.restclients.AgifyClient;
import com.hng.nameprocessing.services.restclients.GenderizeClient;
import com.hng.nameprocessing.services.restclients.NationalizeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class ControllerFailureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NameProcessingController nameProcessingController;

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
                null,
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
    public void processNameShouldReturn502() throws Exception {
        // Given
        String name = "edward";

        RequestModel requestModel = new RequestModel(name);

        String content = objectMapper.writeValueAsString(requestModel);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().is(502))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}
