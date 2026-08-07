package com.bookstore.books.service;

import com.bookstore.books.author.dto.AuthorRequest;
import com.bookstore.books.author.dto.AuthorResponse;
import com.bookstore.books.author.entity.Author;
import com.bookstore.books.author.repository.AuthorRepository;
import com.bookstore.books.author.service.AuthorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    @Test
    void GivenValidRequest_WhenCreateAuthor_ThenPersistAndReturnResponse() {
        AuthorRequest request = AuthorRequest.builder()
                .firstName("Jane")
                .lastName("Austen")
                .biography("Novelist")
                .country("UK")
                .build();

        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(UUID.randomUUID());
            return author;
        });

        AuthorResponse response = authorService.createAuthor(request);

        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getLastName()).isEqualTo("Austen");
        assertThat(response.getId()).isNotNull();

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(captor.capture());
        assertThat(captor.getValue().getCountry()).isEqualTo("UK");
    }

    @Test
    void GivenExistingAuthor_WhenUpdateAuthor_ThenApplyChanges() {
        UUID id = UUID.randomUUID();
        Author existing = Author.builder()
                .id(id)
                .firstName("Old")
                .lastName("Name")
                .build();
        AuthorRequest request = AuthorRequest.builder()
                .firstName("New")
                .lastName("Name")
                .biography("Updated")
                .country("US")
                .build();

        when(authorRepository.findById(id)).thenReturn(Optional.of(existing));

        AuthorResponse response = authorService.updateAuthor(request, id);

        assertThat(response.getFirstName()).isEqualTo("New");
        assertThat(response.getBiography()).isEqualTo("Updated");
        verify(authorRepository, never()).save(any());
    }

    @Test
    void GivenUnknownId_WhenUpdateAuthor_ThenThrowRuntimeException() {
        UUID id = UUID.randomUUID();
        AuthorRequest request = AuthorRequest.builder().firstName("A").lastName("B").build();
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.updateAuthor(request, id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Author not found");
    }

    @Test
    void GivenExistingAuthor_WhenGetAuthorById_ThenReturnResponse() {
        UUID id = UUID.randomUUID();
        Author author = Author.builder().id(id).firstName("Jane").lastName("Doe").build();
        when(authorRepository.findById(id)).thenReturn(Optional.of(author));

        AuthorResponse response = authorService.getAuthorById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void GivenUnknownId_WhenGetAuthorById_ThenThrowRuntimeException() {
        UUID id = UUID.randomUUID();
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.getAuthorById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Author not found");
    }

    @Test
    void GivenAuthorId_WhenDeleteAuthorById_ThenDelegateToRepository() {
        UUID id = UUID.randomUUID();

        authorService.deleteAuthorById(id);

        verify(authorRepository).deleteById(id);
    }

    @Test
    void GivenAuthorsExist_WhenGetAllAuthors_ThenReturnMappedList() {
        Author author = Author.builder()
                .id(UUID.randomUUID())
                .firstName("A")
                .lastName("B")
                .build();
        when(authorRepository.findAll()).thenReturn(List.of(author));

        List<AuthorResponse> responses = authorService.getAllAuthors();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getLastName()).isEqualTo("B");
    }
}
