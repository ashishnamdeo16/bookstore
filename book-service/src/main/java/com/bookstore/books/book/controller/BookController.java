package com.bookstore.books.book.controller;

import com.bookstore.books.book.dto.BookCreateRequest;
import com.bookstore.books.book.dto.BookResponse;
import com.bookstore.books.book.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookCreateRequest request) {
        return new ResponseEntity<>(bookService.createBook(request), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> updateBook(
            @Valid @RequestBody BookCreateRequest request,
            @PathVariable UUID id
    ) {
        return new ResponseEntity<>(bookService.updateBook(request, id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteBook(@PathVariable UUID id) {
        bookService.deleteBookById(id);
        return new ResponseEntity<>("Deleted SuccessFully", HttpStatus.OK);
    }

    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> uploadCover(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(bookService.uploadCover(id, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable UUID id) {
        return new ResponseEntity<>(bookService.getBookById(id), HttpStatus.OK);
    }

    @PostMapping("/batch")
    public List<BookResponse> getBooksByIds(@RequestBody List<UUID> bookIds) {
        return bookService.getBooksByIds(bookIds);
    }

    @GetMapping("/")
    public ResponseEntity<List<BookResponse>> getAllBook() {
        return new ResponseEntity<>(bookService.getAllBooks(), HttpStatus.OK);
    }
}
