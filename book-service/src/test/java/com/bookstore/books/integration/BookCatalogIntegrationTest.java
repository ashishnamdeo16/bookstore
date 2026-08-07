package com.bookstore.books.integration;

import com.bookstore.books.author.dto.AuthorRequest;
import com.bookstore.books.book.dto.BookCreateRequest;
import com.bookstore.books.book.repository.BookRepository;
import com.bookstore.books.category.dto.CategoryRequest;
import com.bookstore.books.publisher.dto.PublisherRequest;
import com.bookstore.books.support.MySQLTestcontainers;
import com.fasterxml.jackson.databind.JsonNode;
import com.bookstore.books.support.TestObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookCatalogIntegrationTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = TestObjectMapperFactory.create();
    @Autowired private BookRepository bookRepository;

    @MockitoBean
    private S3Client s3Client;

    @BeforeEach
    void cleanBooks() {
        bookRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void GivenAdminWorkflow_WhenCreateCatalogEntitiesAndBookWithCover_ThenPersistAndExposeCoverUrl()
            throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        UUID authorId = createAuthor();
        UUID categoryId = createCategory();
        UUID publisherId = createPublisher();

        BookCreateRequest createRequest = BookCreateRequest.builder()
                .isbn("9780132350884")
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .price(new BigDecimal("29.99"))
                .language("English")
                .publishedDate(LocalDate.of(2008, 8, 1))
                .categoryId(categoryId)
                .publisherId(publisherId)
                .authorIds(Set.of(authorId))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/books/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andReturn();

        UUID bookId = UUID.fromString(
                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        MockMultipartFile cover = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", new byte[]{10, 20, 30});

        mockMvc.perform(multipart("/api/books/{id}/cover", bookId)
                        .file(cover)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImageUrl").value(org.hamcrest.Matchers.containsString(
                        "https://test-bookstore-book-images.s3.us-west-2.amazonaws.com/covers/")));

        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.coverImageUrl").isNotEmpty())
                .andExpect(jsonPath("$.authorIds[0]").value(authorId.toString()));

        assertThat(bookRepository.findById(bookId)).isPresent();
        assertThat(bookRepository.findById(bookId).orElseThrow().getCoverImageUrl())
                .contains("covers/");
    }

    @Test
    @WithMockUser(roles = "USER")
    void GivenUserRole_WhenCreateBook_ThenReturn403() throws Exception {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("9780132350884")
                .title("Clean Code")
                .price(new BigDecimal("29.99"))
                .language("English")
                .categoryId(UUID.randomUUID())
                .publisherId(UUID.randomUUID())
                .authorIds(Set.of(UUID.randomUUID()))
                .build();

        mockMvc.perform(post("/api/books/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    private UUID createAuthor() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/authors/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                AuthorRequest.builder().firstName("Robert").lastName("Martin").build())))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private UUID createCategory() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                CategoryRequest.builder().name("Software").description("Tech").build())))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private UUID createPublisher() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/publishers/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                PublisherRequest.builder().name("Prentice Hall").address("NJ").build())))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private UUID readId(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(node.get("id").asText());
    }
}
