package com.bookstore.books.author.controller;

import com.bookstore.books.author.dto.AuthorRequest;
import com.bookstore.books.author.dto.AuthorResponse;
import com.bookstore.books.author.service.AuthorServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorServiceImpl authorService;

    public AuthorController(AuthorServiceImpl authorService) {
        this.authorService = authorService;
    }

    @PostMapping("/create")
    public ResponseEntity<AuthorResponse> createAuthor(@RequestBody AuthorRequest request){
        return new ResponseEntity<>(authorService.createAuthor(request), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(@RequestBody AuthorRequest request, @PathVariable UUID id){
        return new ResponseEntity<>(authorService.updateAuthor(request,id), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable UUID id){
        return new ResponseEntity<>(authorService.getAuthorById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAuthorById(@PathVariable UUID id){
        authorService.deleteAuthorById(id);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<List<AuthorResponse>> getAllAuthors(){
        return new ResponseEntity<>(authorService.getAllAuthors(), HttpStatus.OK);
    }

}
