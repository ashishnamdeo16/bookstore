package com.bookstore.books.repository;

import com.bookstore.books.author.entity.Author;
import com.bookstore.books.author.repository.AuthorRepository;
import com.bookstore.books.book.entity.Book;
import com.bookstore.books.book.repository.BookRepository;
import com.bookstore.books.category.entity.Category;
import com.bookstore.books.category.repository.CategoryRepository;
import com.bookstore.books.publisher.entity.Publisher;
import com.bookstore.books.publisher.repository.PublisherRepository;
import com.bookstore.books.support.MySQLTestcontainers;
import com.bookstore.books.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class BookRepositoryTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired private BookRepository bookRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PublisherRepository publisherRepository;

    private Category category;
    private Publisher publisher;
    private Author author;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
        publisherRepository.deleteAll();

        category = categoryRepository.save(TestDataFactory.category("Fiction"));
        publisher = publisherRepository.save(TestDataFactory.publisher("Penguin"));
        author = authorRepository.save(TestDataFactory.author("Jane", "Austen"));
    }

    @Test
    void GivenNewBook_WhenSave_ThenGenerateIdAndPersistRelationships() {
        Book book = TestDataFactory.book(
                "9780141439518", "Pride and Prejudice", category, publisher, Set.of(author));

        Book saved = bookRepository.save(book);

        assertThat(saved.getId()).isNotNull();
        Optional<Book> loaded = bookRepository.findByIdWithDetails(saved.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getAuthors()).extracting(Author::getId).containsExactly(author.getId());
        assertThat(loaded.get().getCategory().getName()).isEqualTo("Fiction");
        assertThat(loaded.get().getPublisher().getName()).isEqualTo("Penguin");
    }

    @Test
    void GivenDuplicateIsbn_WhenSave_ThenThrowDataIntegrityViolation() {
        bookRepository.saveAndFlush(TestDataFactory.book(
                "9780141439518", "Book One", category, publisher, Set.of(author)));

        Book duplicate = TestDataFactory.book(
                "9780141439518", "Book Two", category, publisher, Set.of(author));

        assertThatThrownBy(() -> bookRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void GivenMultipleBooks_WhenFindAllWithDetails_ThenReturnAllWithAssociations() {
        bookRepository.save(TestDataFactory.book(
                "9780141439518", "Pride and Prejudice", category, publisher, Set.of(author)));
        bookRepository.save(TestDataFactory.book(
                "9780141439556", "Emma", category, publisher, Set.of(author)));

        List<Book> books = bookRepository.findAllWithDetails();

        assertThat(books).hasSize(2);
        assertThat(books).allSatisfy(book -> {
            assertThat(book.getAuthors()).isNotEmpty();
            assertThat(book.getCategory()).isNotNull();
            assertThat(book.getPublisher()).isNotNull();
        });
    }

    @Test
    void GivenExistingBook_WhenUpdateCoverImageUrl_ThenPersistChange() {
        Book saved = bookRepository.save(TestDataFactory.book(
                "9780141439518", "Pride and Prejudice", category, publisher, Set.of(author)));

        saved.setCoverImageUrl("https://example.com/cover.jpg");
        bookRepository.saveAndFlush(saved);

        Book reloaded = bookRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getCoverImageUrl()).isEqualTo("https://example.com/cover.jpg");
    }

    @Test
    void GivenExistingBook_WhenDeleteById_ThenRemoved() {
        Book saved = bookRepository.save(TestDataFactory.book(
                "9780141439518", "Pride and Prejudice", category, publisher, Set.of(author)));
        UUID id = saved.getId();

        bookRepository.deleteById(id);

        assertThat(bookRepository.findById(id)).isEmpty();
    }

    @Test
    void GivenBookIds_WhenFindAllById_ThenReturnMatchingSubset() {
        Book first = bookRepository.save(TestDataFactory.book(
                "9780141439518", "Pride and Prejudice", category, publisher, Set.of(author)));
        bookRepository.save(TestDataFactory.book(
                "9780141439556", "Emma", category, publisher, Set.of(author)));

        List<Book> found = bookRepository.findAllById(List.of(first.getId()));

        assertThat(found).extracting(Book::getId).containsExactly(first.getId());
    }
}
