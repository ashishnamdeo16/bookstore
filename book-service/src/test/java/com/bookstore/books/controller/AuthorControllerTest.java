package com.bookstore.books.controller;

import com.bookstore.books.author.controller.AuthorController;
import com.bookstore.books.author.dto.AuthorRequest;
import com.bookstore.books.author.dto.AuthorResponse;
import com.bookstore.books.author.service.AuthorServiceImpl;
import com.bookstore.books.config.SecurityConfig;
import com.bookstore.books.exception.GlobalExceptionHandler;
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

@WebMvcTest(controllers = AuthorController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class AuthorControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = TestObjectMapperFactory.create();

    @MockitoBean private AuthorServiceImpl authorService;
    @MockitoBean private JwtService jwtService;

    @Test
    void GivenNoAuth_WhenListAuthors_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/authors/")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUser_WhenCreateAuthor_ThenReturn403() throws Exception {
        AuthorRequest request = AuthorRequest.builder().firstName("Jane").lastName("Austen").build();

        mockMvc.perform(post("/api/authors/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenCreateAuthor_ThenReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        AuthorRequest request = AuthorRequest.builder().firstName("Jane").lastName("Austen").build();
        when(authorService.createAuthor(any())).thenReturn(
                AuthorResponse.builder().id(id).firstName("Jane").lastName("Austen").build());

        mockMvc.perform(post("/api/authors/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenBlankFirstName_WhenCreateAuthor_ThenReturn400() throws Exception {
        AuthorRequest request = AuthorRequest.builder().firstName(" ").build();

        mockMvc.perform(post("/api/authors/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUser_WhenGetAuthor_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(authorService.getAuthorById(id)).thenReturn(
                AuthorResponse.builder().id(id).firstName("Jane").lastName("Austen").build());

        mockMvc.perform(get("/api/authors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenUpdateAuthor_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        AuthorRequest request = AuthorRequest.builder().firstName("Jane").lastName("Austen").build();
        when(authorService.updateAuthor(any(), eq(id))).thenReturn(
                AuthorResponse.builder().id(id).firstName("Jane").lastName("Austen").build());

        mockMvc.perform(put("/api/authors/update/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenDeleteAuthor_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/authors/{id}", id).with(csrf()))
                .andExpect(status().isOk());

        verify(authorService).deleteAuthorById(id);
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUser_WhenListAuthors_ThenReturn200() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(List.of(
                AuthorResponse.builder().id(UUID.randomUUID()).firstName("Jane").lastName("Austen").build()));

        mockMvc.perform(get("/api/authors/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Jane"));
    }
}
