package com.bookstore.books.controller;

import com.bookstore.books.category.controller.CategoryController;
import com.bookstore.books.category.dto.CategoryRequest;
import com.bookstore.books.category.dto.CategoryResponse;
import com.bookstore.books.category.service.CategoryServiceImpl;
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

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = TestObjectMapperFactory.create();

    @MockitoBean private CategoryServiceImpl categoryService;
    @MockitoBean private JwtService jwtService;

    @Test
    void GivenNoAuth_WhenListCategories_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/categories/")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUser_WhenCreateCategory_ThenReturn403() throws Exception {
        CategoryRequest request = CategoryRequest.builder().name("Fiction").build();

        mockMvc.perform(post("/api/categories/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenCreateCategory_ThenReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.createCategory(any())).thenReturn(
                CategoryResponse.builder().id(id).name("Fiction").build());

        mockMvc.perform(post("/api/categories/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                CategoryRequest.builder().name("Fiction").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fiction"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUser_WhenGetCategory_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.getById(id)).thenReturn(
                CategoryResponse.builder().id(id).name("Fiction").build());

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenUpdateCategory_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.updateCategory(any(), eq(id))).thenReturn(
                CategoryResponse.builder().id(id).name("Updated").build());

        mockMvc.perform(put("/api/categories/update/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                CategoryRequest.builder().name("Updated").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenDeleteCategory_ThenReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/categories/{id}", id).with(csrf()))
                .andExpect(status().isOk());
        verify(categoryService).deleteById(id);
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUser_WhenListCategories_ThenReturn200() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(
                CategoryResponse.builder().id(UUID.randomUUID()).name("Fiction").build()));

        mockMvc.perform(get("/api/categories/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fiction"));
    }
}
