package com.bookstore.books.controller;

import com.bookstore.books.config.SecurityConfig;
import com.bookstore.books.exception.GlobalExceptionHandler;
import com.bookstore.books.publisher.controller.PublisherController;
import com.bookstore.books.publisher.dto.PublisherRequest;
import com.bookstore.books.publisher.dto.PublisherResponse;
import com.bookstore.books.publisher.service.PublisherServiceImpl;
import com.bookstore.books.security.JwtAuthenticationFilter;
import com.bookstore.books.security.JwtService;
import com.bookstore.books.support.TestObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublisherController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class PublisherControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = TestObjectMapperFactory.create();

    @MockitoBean private PublisherServiceImpl publisherService;
    @MockitoBean private JwtService jwtService;

    @Test
    void GivenNoAuth_WhenListPublishers_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/publishers/")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUser_WhenCreatePublisher_ThenReturn403() throws Exception {
        mockMvc.perform(post("/api/publishers/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                PublisherRequest.builder().name("Penguin").build())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenCreatePublisher_ThenReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        when(publisherService.createPublisher(any())).thenReturn(
                PublisherResponse.builder().id(id).name("Penguin").build());

        mockMvc.perform(post("/api/publishers/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                PublisherRequest.builder().name("Penguin").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Penguin"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUser_WhenGetPublisher_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(publisherService.getPublisherById(id)).thenReturn(
                PublisherResponse.builder().id(id).name("Penguin").build());

        mockMvc.perform(get("/api/publishers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenUpdatePublisher_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(publisherService.updatePublisher(any(), eq(id))).thenReturn(
                PublisherResponse.builder().id(id).name("Updated").build());

        mockMvc.perform(put("/api/publishers/update/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                PublisherRequest.builder().name("Updated").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenDeletePublisher_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/publishers/{id}", id).with(csrf()))
                .andExpect(status().isOk());
        verify(publisherService).deletePublisherById(id);
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUser_WhenListPublishers_ThenReturn200() throws Exception {
        when(publisherService.getAllPublishers()).thenReturn(List.of(
                PublisherResponse.builder().id(UUID.randomUUID()).name("Penguin").build()));

        mockMvc.perform(get("/api/publishers/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Penguin"));
    }
}
