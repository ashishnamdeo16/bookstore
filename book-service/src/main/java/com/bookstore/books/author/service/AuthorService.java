package com.bookstore.books.author.service;

import com.bookstore.books.author.dto.AuthorRequest;
import com.bookstore.books.author.dto.AuthorResponse;

import java.util.List;
import java.util.UUID;

public interface AuthorService {

    AuthorResponse createAuthor(AuthorRequest request);

    AuthorResponse updateAuthor(AuthorRequest request, UUID id);

    AuthorResponse getAuthorById(UUID id);

    void deleteAuthorById(UUID id);

    List<AuthorResponse> getAllAuthors();

}
