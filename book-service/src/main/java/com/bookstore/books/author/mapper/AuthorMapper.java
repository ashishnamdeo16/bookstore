package com.bookstore.books.author.mapper;

import com.bookstore.books.author.dto.AuthorResponse;
import com.bookstore.books.author.entity.Author;

public class AuthorMapper {

    private AuthorMapper() {
        /* This utility class should not be instantiated */
    }

    public static AuthorResponse toResponse(Author author){

        return AuthorResponse
                .builder()
                .id(author.getId())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .biography(author.getBiography())
                .country(author.getCountry())
                .build();
    }
}
