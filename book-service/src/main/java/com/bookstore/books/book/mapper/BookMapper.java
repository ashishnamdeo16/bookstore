package com.bookstore.books.book.mapper;

import com.bookstore.books.author.entity.Author;
import com.bookstore.books.book.dto.BookResponse;
import com.bookstore.books.book.entity.Book;

import java.util.stream.Collectors;

public class BookMapper {

    private BookMapper() {
        /* This utility class should not be instantiated */
    }

    public static BookResponse toResponse(Book book){

        return BookResponse
                .builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .title(book.getTitle())
                .price(book.getPrice())
                .language(book.getLanguage())
                .publishedDate(book.getPublishedDate())
                .publisherId(book.getPublisher().getId())
                .categoryId(book.getCategory().getId())
                .authorIds(book.getAuthors()
                        .stream()
                        .map(Author::getId)
                        .collect(Collectors.toSet()))
                .build();
    }
}
