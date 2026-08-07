package com.bookstore.books.support;

import com.bookstore.books.author.entity.Author;
import com.bookstore.books.book.dto.BookCreateRequest;
import com.bookstore.books.book.entity.Book;
import com.bookstore.books.category.entity.Category;
import com.bookstore.books.publisher.entity.Publisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Category category(String name) {
        return Category.builder()
                .name(name)
                .description(name + " description")
                .build();
    }

    public static Publisher publisher(String name) {
        return Publisher.builder()
                .name(name)
                .address("123 Publisher St")
                .build();
    }

    public static Author author(String firstName, String lastName) {
        return Author.builder()
                .firstName(firstName)
                .lastName(lastName)
                .biography("Bio")
                .country("US")
                .build();
    }

    public static Book book(String isbn, String title, Category category, Publisher publisher, Set<Author> authors) {
        return Book.builder()
                .isbn(isbn)
                .title(title)
                .description("A great book")
                .price(new BigDecimal("19.99"))
                .language("English")
                .publishedDate(LocalDate.of(2024, 1, 15))
                .category(category)
                .publisher(publisher)
                .authors(authors == null ? new HashSet<>() : new HashSet<>(authors))
                .build();
    }

    public static BookCreateRequest bookCreateRequest(
            String isbn,
            UUID categoryId,
            UUID publisherId,
            Set<UUID> authorIds
    ) {
        return BookCreateRequest.builder()
                .isbn(isbn)
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .price(new BigDecimal("29.99"))
                .language("English")
                .publishedDate(LocalDate.of(2008, 8, 1))
                .categoryId(categoryId)
                .publisherId(publisherId)
                .authorIds(authorIds)
                .build();
    }
}
