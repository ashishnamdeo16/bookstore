package com.bookstore.books.book.service;

import com.bookstore.books.author.entity.Author;
import com.bookstore.books.author.repository.AuthorRepository;
import com.bookstore.books.book.dto.BookCreateRequest;
import com.bookstore.books.book.dto.BookResponse;
import com.bookstore.books.book.entity.Book;
import com.bookstore.books.book.mapper.BookMapper;
import com.bookstore.books.book.repository.BookRepository;
import com.bookstore.books.book.storage.BookCoverStorageService;
import com.bookstore.books.category.entity.Category;
import com.bookstore.books.category.repository.CategoryRepository;
import com.bookstore.books.exception.ResourceNotFoundException;
import com.bookstore.books.publisher.entity.Publisher;
import com.bookstore.books.publisher.repository.PublisherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {

    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;
    private final BookCoverStorageService bookCoverStorageService;
    private final com.bookstore.books.observability.BusinessMetrics businessMetrics;

    public BookServiceImpl(
            CategoryRepository categoryRepository,
            AuthorRepository authorRepository,
            PublisherRepository publisherRepository,
            BookRepository bookRepository,
            BookCoverStorageService bookCoverStorageService,
            com.bookstore.books.observability.BusinessMetrics businessMetrics
    ) {
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.bookRepository = bookRepository;
        this.bookCoverStorageService = bookCoverStorageService;
        this.businessMetrics = businessMetrics;
    }

    @Override
    public BookResponse createBook(BookCreateRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new ResourceNotFoundException("Publisher not found"));

        Set<Author> authors = new HashSet<>(authorRepository.findAllById(request.getAuthorIds()));

        if (authors.size() != request.getAuthorIds().size()) {
            throw new ResourceNotFoundException("One or more authors not found");
        }

        Book book = Book.builder()
                .isbn(request.getIsbn())
                .description(request.getDescription())
                .title(request.getTitle())
                .price(request.getPrice())
                .language(request.getLanguage())
                .publishedDate(request.getPublishedDate())
                .category(category)
                .publisher(publisher)
                .authors(authors)
                .build();

        Book savedBook = bookRepository.save(book);
        businessMetrics.recordBookCreated();

        return BookMapper.toResponse(savedBook);
    }

    @Override
    public BookResponse updateBook(BookCreateRequest request, UUID id) {

        Book book = bookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new ResourceNotFoundException("Publisher not found"));

        Set<Author> authors = new HashSet<>(authorRepository.findAllById(request.getAuthorIds()));

        if (authors.size() != request.getAuthorIds().size()) {
            throw new ResourceNotFoundException("One or more authors not found");
        }

        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setPrice(request.getPrice());
        book.setCategory(category);
        book.setPublisher(publisher);
        book.setAuthors(authors);

        Book savedBook = bookRepository.save(book);

        return BookMapper.toResponse(savedBook);
    }

    @Override
    public BookResponse getBookById(UUID id) {
        Book book = bookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        return BookMapper.toResponse(book);
    }

    @Override
    public List<BookResponse> getBooksByIds(List<UUID> bookIds) {

        List<Book> books = bookRepository.findAllById(bookIds);

        return books.stream()
                .map(BookMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteBookById(UUID id) {
         bookRepository.deleteById(id);
    }

    @Override
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.findAllWithDetails();
        return books
                .stream()
                .map(BookMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BookResponse uploadCover(UUID id, MultipartFile file) {
        Book book = bookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        String coverImageUrl = bookCoverStorageService.uploadCover(id, file);
        book.setCoverImageUrl(coverImageUrl);

        Book savedBook = bookRepository.save(book);
        return BookMapper.toResponse(savedBook);
    }
}
