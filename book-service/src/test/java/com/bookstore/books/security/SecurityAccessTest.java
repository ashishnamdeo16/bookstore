package com.bookstore.books.security;

import com.bookstore.books.book.controller.BookController;
import com.bookstore.books.book.dto.BookResponse;
import com.bookstore.books.book.service.BookService;
import com.bookstore.books.config.SecurityConfig;
import com.bookstore.books.exception.GlobalExceptionHandler;
import com.bookstore.books.security.JwtAuthenticationFilter;
import com.bookstore.books.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void GivenAnonymousCaller_WhenAccessProtectedEndpoint_ThenReturn401() throws Exception {
        mockMvc.perform(get("/api/books/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenAuthenticatedUser_WhenAccessReadEndpoint_ThenReturn200() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(
                BookResponse.builder().id(UUID.randomUUID()).title("Clean Code").build()));

        mockMvc.perform(get("/api/books/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void GivenHealthEndpoint_WhenAnonymous_ThenStillRequireAuthBecauseNotPermitAllForBooks() throws Exception {
        // Actuator is permitAll; books are not. Assert books remain protected.
        mockMvc.perform(get("/api/books/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
