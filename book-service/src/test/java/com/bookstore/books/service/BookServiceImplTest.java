package com.bookstore.books.service;

import com.bookstore.books.author.entity.Author;
import com.bookstore.books.author.repository.AuthorRepository;
import com.bookstore.books.book.dto.BookCreateRequest;
import com.bookstore.books.book.dto.BookResponse;
import com.bookstore.books.book.entity.Book;
import com.bookstore.books.book.repository.BookRepository;
import com.bookstore.books.book.service.BookServiceImpl;
import com.bookstore.books.book.storage.BookCoverStorageService;
import com.bookstore.books.category.entity.Category;
import com.bookstore.books.category.repository.CategoryRepository;
import com.bookstore.books.exception.ResourceNotFoundException;
import com.bookstore.books.publisher.entity.Publisher;
import com.bookstore.books.publisher.repository.PublisherRepository;
import com.bookstore.books.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private PublisherRepository publisherRepository;
    @Mock private BookRepository bookRepository;
    @Mock private BookCoverStorageService bookCoverStorageService;
    @Mock private com.bookstore.books.observability.BusinessMetrics businessMetrics;

    @InjectMocks
    private BookServiceImpl bookService;

    private UUID categoryId;
    private UUID publisherId;
    private UUID authorId;
    private Category category;
    private Publisher publisher;
    private Author author;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        publisherId = UUID.randomUUID();
        authorId = UUID.randomUUID();

        category = TestDataFactory.category("Fiction");
        category.setId(categoryId);

        publisher = TestDataFactory.publisher("O'Reilly");
        publisher.setId(publisherId);

        author = TestDataFactory.author("Robert", "Martin");
        author.setId(authorId);
    }

    @Test
    void GivenValidRequest_WhenCreateBook_ThenPersistAndReturnResponse() {
        BookCreateRequest request = TestDataFactory.bookCreateRequest(
                "9780132350884", categoryId, publisherId, Set.of(authorId));

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(authorId))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(UUID.randomUUID());
            return book;
        });

        BookResponse response = bookService.createBook(request);

        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getIsbn()).isEqualTo("9780132350884");
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getPublisherId()).isEqualTo(publisherId);
        assertThat(response.getAuthorIds()).containsExactly(authorId);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getPrice()).isEqualByComparingTo("29.99");
    }

    @Test
    void GivenMissingCategory_WhenCreateBook_ThenThrowResourceNotFoundException() {
        BookCreateRequest request = TestDataFactory.bookCreateRequest(
                "9780132350884", categoryId, publisherId, Set.of(authorId));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void GivenMissingPublisher_WhenCreateBook_ThenThrowResourceNotFoundException() {
        BookCreateRequest request = TestDataFactory.bookCreateRequest(
                "9780132350884", categoryId, publisherId, Set.of(authorId));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(publisherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Publisher not found");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void GivenMissingAuthor_WhenCreateBook_ThenThrowResourceNotFoundException() {
        BookCreateRequest request = TestDataFactory.bookCreateRequest(
                "9780132350884", categoryId, publisherId, Set.of(authorId));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(authorId))).thenReturn(List.of());

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("One or more authors not found");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void GivenExistingBook_WhenUpdateBook_ThenPersistChanges() {
        UUID bookId = UUID.randomUUID();
        Book existing = TestDataFactory.book(
                "9780132350884", "Old Title", category, publisher, Set.of(author));
        existing.setId(bookId);

        BookCreateRequest request = TestDataFactory.bookCreateRequest(
                "9780132350884", categoryId, publisherId, Set.of(authorId));
        request.setTitle("New Title");
        request.setPrice(new BigDecimal("39.99"));

        when(bookRepository.findByIdWithDetails(bookId)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(authorId))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.updateBook(request, bookId);

        assertThat(response.getTitle()).isEqualTo("New Title");
        assertThat(response.getPrice()).isEqualByComparingTo("39.99");
        verify(bookRepository).save(existing);
    }

    @Test
    void GivenUnknownBookId_WhenUpdateBook_ThenThrowResourceNotFoundException() {
        UUID bookId = UUID.randomUUID();
        BookCreateRequest request = TestDataFactory.bookCreateRequest(
                "9780132350884", categoryId, publisherId, Set.of(authorId));
        when(bookRepository.findByIdWithDetails(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(request, bookId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book not found");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void GivenExistingBook_WhenGetBookById_ThenReturnMappedResponse() {
        UUID bookId = UUID.randomUUID();
        Book book = TestDataFactory.book("9780132350884", "Clean Code", category, publisher, Set.of(author));
        book.setId(bookId);
        book.setCoverImageUrl("https://example.com/cover.jpg");

        when(bookRepository.findByIdWithDetails(bookId)).thenReturn(Optional.of(book));

        BookResponse response = bookService.getBookById(bookId);

        assertThat(response.getId()).isEqualTo(bookId);
        assertThat(response.getCoverImageUrl()).isEqualTo("https://example.com/cover.jpg");
        assertThat(response.getAuthorIds()).containsExactly(authorId);
    }

    @Test
    void GivenUnknownBookId_WhenGetBookById_ThenThrowResourceNotFoundException() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findByIdWithDetails(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(bookId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book not found");
    }

    @Test
    void GivenBooksInCatalog_WhenGetAllBooks_ThenReturnAllMapped() {
        Book book = TestDataFactory.book("9780132350884", "Clean Code", category, publisher, Set.of(author));
        book.setId(UUID.randomUUID());
        when(bookRepository.findAllWithDetails()).thenReturn(List.of(book));

        List<BookResponse> responses = bookService.getAllBooks();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Clean Code");
        verify(bookRepository, times(1)).findAllWithDetails();
    }

    @Test
    void GivenBookIds_WhenGetBooksByIds_ThenReturnMatchingBooks() {
        UUID bookId = UUID.randomUUID();
        Book book = TestDataFactory.book("9780132350884", "Clean Code", category, publisher, Set.of(author));
        book.setId(bookId);
        when(bookRepository.findAllById(List.of(bookId))).thenReturn(List.of(book));

        List<BookResponse> responses = bookService.getBooksByIds(List.of(bookId));

        assertThat(responses).extracting(BookResponse::getId).containsExactly(bookId);
    }

    @Test
    void GivenBookId_WhenDeleteBookById_ThenDelegateToRepository() {
        UUID bookId = UUID.randomUUID();

        bookService.deleteBookById(bookId);

        verify(bookRepository).deleteById(bookId);
    }

    @Test
    void GivenValidCover_WhenUploadCover_ThenPersistCoverUrl() {
        UUID bookId = UUID.randomUUID();
        Book book = TestDataFactory.book("9780132350884", "Clean Code", category, publisher, Set.of(author));
        book.setId(bookId);

        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(bookRepository.findByIdWithDetails(bookId)).thenReturn(Optional.of(book));
        when(bookCoverStorageService.uploadCover(bookId, file))
                .thenReturn("https://bucket.s3.us-west-2.amazonaws.com/covers/cover.jpg");
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.uploadCover(bookId, file);

        assertThat(response.getCoverImageUrl())
                .isEqualTo("https://bucket.s3.us-west-2.amazonaws.com/covers/cover.jpg");
        verify(bookCoverStorageService).uploadCover(bookId, file);
        verify(bookRepository).save(book);
    }

    @Test
    void GivenUnknownBook_WhenUploadCover_ThenThrowResourceNotFoundException() {
        UUID bookId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(bookRepository.findByIdWithDetails(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.uploadCover(bookId, file))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book not found");

        verify(bookCoverStorageService, never()).uploadCover(any(), any());
        verify(bookRepository, never()).save(any());
    }
}
