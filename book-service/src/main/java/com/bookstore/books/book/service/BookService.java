package com.bookstore.books.book.service;

import com.bookstore.books.book.dto.BookCreateRequest;
import com.bookstore.books.book.dto.BookResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BookService {

    BookResponse createBook(BookCreateRequest request);

    BookResponse updateBook(BookCreateRequest request, UUID id);

    BookResponse getBookById(UUID id);

    void deleteBookById(UUID id);

    List<BookResponse> getAllBooks();

    List<BookResponse> getBooksByIds(List<UUID> bookIds);

    BookResponse uploadCover(UUID id, MultipartFile file);
}
