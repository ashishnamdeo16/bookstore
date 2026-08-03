package com.bookstore.books.author.service;

import com.bookstore.books.author.dto.AuthorRequest;
import com.bookstore.books.author.dto.AuthorResponse;
import com.bookstore.books.author.entity.Author;
import com.bookstore.books.author.mapper.AuthorMapper;
import com.bookstore.books.author.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository){
        this.authorRepository =  authorRepository;
    }

    @Override
    public AuthorResponse createAuthor(AuthorRequest request) {

        Author author = Author
                .builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .biography(request.getBiography())
                .country(request.getCountry())
                .build();

        Author savedAuthor = authorRepository.save(author);

        return AuthorMapper.toResponse(savedAuthor);
    }

    @Override
    public AuthorResponse updateAuthor(AuthorRequest request, UUID id) {

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        author.setFirstName(request.getFirstName());
        author.setLastName(request.getLastName());
        author.setBiography(request.getBiography());
        author.setCountry(request.getCountry());

        return AuthorMapper.toResponse(author);

    }

    @Override
    public AuthorResponse getAuthorById(UUID id) {

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        return AuthorMapper.toResponse(author);

    }

    @Override
    public void deleteAuthorById(UUID id) {
        authorRepository.deleteById(id);
    }

    @Override
    public List<AuthorResponse> getAllAuthors() {
        return authorRepository.findAll().stream().map(AuthorMapper::toResponse).toList();
    }
}
