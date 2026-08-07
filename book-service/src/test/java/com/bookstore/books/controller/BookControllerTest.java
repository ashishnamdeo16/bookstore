package com.bookstore.books.controller;

import com.bookstore.books.book.controller.BookController;
import com.bookstore.books.book.dto.BookCreateRequest;
import com.bookstore.books.book.dto.BookResponse;
import com.bookstore.books.book.service.BookService;
import com.bookstore.books.config.SecurityConfig;
import com.bookstore.books.exception.GlobalExceptionHandler;
import com.bookstore.books.exception.ResourceNotFoundException;
import com.bookstore.books.security.JwtAuthenticationFilter;
import com.bookstore.books.security.JwtService;
import com.bookstore.books.support.TestDataFactory;
import com.bookstore.books.support.TestObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = TestObjectMapperFactory.create();

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void GivenNoAuth_WhenGetBooks_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/books/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUserRole_WhenCreateBook_ThenReturn403() throws Exception {
        BookCreateRequest request = TestDataFactory.bookCreateRequest(
                "9780132350884", UUID.randomUUID(), UUID.randomUUID(), Set.of(UUID.randomUUID()));

        mockMvc.perform(post("/api/books/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(bookService, never()).createBook(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdminAndValidBody_WhenCreateBook_ThenReturn201() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        BookCreateRequest request = TestDataFactory.bookCreateRequest(
                "9780132350884", categoryId, publisherId, Set.of(authorId));

        when(bookService.createBook(any(BookCreateRequest.class))).thenReturn(
                BookResponse.builder()
                        .id(bookId)
                        .isbn(request.getIsbn())
                        .title(request.getTitle())
                        .price(request.getPrice())
                        .language(request.getLanguage())
                        .categoryId(categoryId)
                        .publisherId(publisherId)
                        .authorIds(Set.of(authorId))
                        .build());

        mockMvc.perform(post("/api/books/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenInvalidBody_WhenCreateBook_ThenReturn400() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("")
                .title("")
                .price(BigDecimal.valueOf(-1))
                .language("")
                .build();

        mockMvc.perform(post("/api/books/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenAuthenticatedUser_WhenGetBookById_ThenReturn200() throws Exception {
        UUID bookId = UUID.randomUUID();
        when(bookService.getBookById(bookId)).thenReturn(
                BookResponse.builder().id(bookId).title("Clean Code").isbn("9780132350884").build());

        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUnknownBook_WhenGetBookById_ThenReturn404() throws Exception {
        UUID bookId = UUID.randomUUID();
        when(bookService.getBookById(bookId)).thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenUpdateBook_ThenReturn200() throws Exception {
        UUID bookId = UUID.randomUUID();
        BookCreateRequest request = TestDataFactory.bookCreateRequest(
                "9780132350884", UUID.randomUUID(), UUID.randomUUID(), Set.of(UUID.randomUUID()));
        when(bookService.updateBook(any(BookCreateRequest.class), eq(bookId))).thenReturn(
                BookResponse.builder().id(bookId).title("Clean Code").build());

        mockMvc.perform(put("/api/books/update/{id}", bookId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdmin_WhenDeleteBook_ThenReturn200() throws Exception {
        UUID bookId = UUID.randomUUID();

        mockMvc.perform(delete("/api/books/{id}", bookId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted SuccessFully"));

        verify(bookService).deleteBookById(bookId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenAuthenticatedUser_WhenGetAllBooks_ThenReturn200() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(
                BookResponse.builder().id(UUID.randomUUID()).title("Clean Code").build()));

        mockMvc.perform(get("/api/books/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenBookIds_WhenBatchLookup_ThenReturn200() throws Exception {
        UUID bookId = UUID.randomUUID();
        when(bookService.getBooksByIds(List.of(bookId))).thenReturn(List.of(
                BookResponse.builder().id(bookId).title("Clean Code").build()));

        mockMvc.perform(post("/api/books/batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(bookId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdminAndCoverFile_WhenUploadCover_ThenReturn200() throws Exception {
        UUID bookId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(bookService.uploadCover(eq(bookId), any())).thenReturn(
                BookResponse.builder()
                        .id(bookId)
                        .coverImageUrl("https://bucket.s3.us-west-2.amazonaws.com/covers/x.jpg")
                        .build());

        mockMvc.perform(multipart("/api/books/{id}/cover", bookId)
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImageUrl").value(containsString("covers/")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenServiceThrowsUnexpected_WhenGetBook_ThenReturn500() throws Exception {
        UUID bookId = UUID.randomUUID();
        when(bookService.getBookById(bookId)).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(containsString("unexpected error")));
    }
}
