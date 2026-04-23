package com.hng.nameprocessing.ControllerTests;

import com.fasterxml.uuid.Generators;
import com.hng.nameprocessing.controllers.NameProcessingController;
import com.hng.nameprocessing.dtos.*;
import com.hng.nameprocessing.repositories.DataRepository;
import com.hng.nameprocessing.services.restclients.AgifyClient;
import com.hng.nameprocessing.services.restclients.GenderizeClient;
import com.hng.nameprocessing.services.restclients.NationalizeClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {

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

    @Autowired
    private DataRepository dataRepository;


//    @Test
//    public void processNameShouldReturn200() throws Exception {
//        // Given
//        String name = "John";
//
//        RequestModel requestModel = new RequestModel(name);
//
//        String content = objectMapper.writeValueAsString(requestModel);
//
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/profiles")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(content))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult))
//                .andExpect(status().isCreated())
//                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
//
//
//        String responseDto = mvcResult.getResponse().getContentAsString();
//
//        System.out.println(responseDto);
//    }
//
//    @Test
//    public void processNameShouldReturn422() throws Exception {
//        // Given
//        String name = "1234";
//
//        RequestModel requestModel = new RequestModel(name);
//
//        String content = objectMapper.writeValueAsString(requestModel);
//
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/profiles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(status().is(422))
//                .andReturn();
//
//    }
//
//    @Test
//    public void processNameShouldReturn400() throws Exception {
//        // Given
//        String name = "";
//
//        RequestModel requestModel = new RequestModel(name);
//
//        String content = objectMapper.writeValueAsString(requestModel);
//
//        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/profiles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(status().is(400))
//                .andReturn();
//    }

    @Test
    public void getProfilesShouldReturn200() throws Exception {
        // When

        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/profiles")
                        .param("gender", "female")
                        .param("page", "2")
                        .param("limit", "15")
                        .param("order", "desc")
                        .param("sort_by", "age")
                        .param("min_gender_probability", "0.8")
                        .param("max_age", "20"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult2))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(mvcResult2.getResponse().getContentAsString());
    }

//    @Test
//    public void getProfilesShouldReturn404() throws Exception {
//        // When
//
//        String name = "Edward";
//
//        RequestModel requestModel = new RequestModel(name);
//
//        String content = objectMapper.writeValueAsString(requestModel);
//
//        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/api/profiles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult1))
//                .andExpect(status().isCreated())
//                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
//
//        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/profiles")
//                        .param("gender", "male")
//                        .param("age_group", "adult")
//                        .param("country_id", "AU"))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult2))
//                .andExpect(status().is(404))
//                .andReturn();
//    }
//
    @Test
    public void getProfileByIdShouldReturn200() throws Exception {
        // When

//        String name = "Edward";
//
//        RequestModel requestModel = new RequestModel(name);
//
//        String content = objectMapper.writeValueAsString(requestModel);
//
//        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/api/profiles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult1))
//                .andExpect(status().isCreated())
//                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
//
//        FreshResponseDto responseDto = objectMapper.readValue(mvcResult1.getResponse().getContentAsString(), FreshResponseDto.class);

        UUID id = UUID.fromString("019db1f4-bac6-70e4-acf2-d1cb3352440a");

        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/profiles/"+id))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult2))
                .andExpect(status().is(200))
                .andReturn();

        System.out.println(mvcResult2.getResponse().getContentAsString());
    }
//
//    @Test
//    public void getProfileByIdShouldReturn500() throws Exception {
//        // When
//
//        String name = "Edward";
//
//        RequestModel requestModel = new RequestModel(name);
//
//        String content = objectMapper.writeValueAsString(requestModel);
//
//        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/api/profiles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult1))
//                .andExpect(status().isCreated())
//                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
//
//        UUID id = Generators.timeBasedEpochGenerator().generate();
//
//        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/profiles/"+id))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult2))
//                .andExpect(status().is(404))
//                .andReturn();
//    }
//
//    @Test
//    public void deleteProfileByIdShouldReturn204() throws Exception {
//        // When
//
//        String name = "Edward";
//
//        RequestModel requestModel = new RequestModel(name);
//
//        String content = objectMapper.writeValueAsString(requestModel);
//
//        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/api/profiles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult1))
//                .andExpect(status().isCreated())
//                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
//
//        FreshResponseDto responseDto = objectMapper.readValue(mvcResult1.getResponse().getContentAsString(), FreshResponseDto.class);
//
//        UUID id = responseDto.getData().getId();
//
//        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.delete("/api/profiles/"+id))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult2))
//                .andExpect(status().is(204))
//                .andReturn();
//    }
//
//    @Test
//    public void deleteProfileByIdShouldReturn404() throws Exception {
//        // When
//
//        String name = "Edward";
//
//        RequestModel requestModel = new RequestModel(name);
//
//        String content = objectMapper.writeValueAsString(requestModel);
//
//        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/api/profiles")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult1))
//                .andExpect(status().isCreated())
//                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
//
//        UUID id = Generators.timeBasedEpochGenerator().generate();
//
//        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.delete("/api/profiles/"+id))
//                .andExpect(request().asyncStarted())
//                .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult2))
//                .andExpect(status().is(404))
//                .andReturn();
//    }
//
//    @Test
//    public void shouldReturnException400() throws Exception {
//        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/api/profiles")
//                )
//                .andExpect(status().is(400))
//                .andReturn();
//    }

}
