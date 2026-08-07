package com.bookstore.books.repository;

import com.bookstore.books.author.entity.Author;
import com.bookstore.books.author.repository.AuthorRepository;
import com.bookstore.books.category.entity.Category;
import com.bookstore.books.category.repository.CategoryRepository;
import com.bookstore.books.publisher.entity.Publisher;
import com.bookstore.books.publisher.repository.PublisherRepository;
import com.bookstore.books.support.MySQLTestcontainers;
import com.bookstore.books.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CatalogRepositoryCrudTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired private AuthorRepository authorRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PublisherRepository publisherRepository;

    @Test
    void GivenAuthor_WhenSaveAndFind_ThenRoundTrip() {
        Author saved = authorRepository.save(TestDataFactory.author("Mark", "Twain"));

        assertThat(saved.getId()).isNotNull();
        assertThat(authorRepository.findById(saved.getId())).isPresent();
        assertThat(authorRepository.findAll()).extracting(Author::getLastName).contains("Twain");
    }

    @Test
    void GivenCategory_WhenUpdate_ThenPersistNewName() {
        Category saved = categoryRepository.save(TestDataFactory.category("Drama"));
        saved.setName("Theater");
        categoryRepository.saveAndFlush(saved);

        Category reloaded = categoryRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Theater");
    }

    @Test
    void GivenPublisher_WhenDelete_ThenRemoved() {
        Publisher saved = publisherRepository.save(TestDataFactory.publisher("Harper"));
        publisherRepository.deleteById(saved.getId());

        assertThat(publisherRepository.findById(saved.getId())).isEmpty();
    }
}
