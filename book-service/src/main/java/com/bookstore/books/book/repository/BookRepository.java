package com.bookstore.books.book.repository;

import com.bookstore.books.book.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    @EntityGraph(attributePaths = {"authors", "category", "publisher"})
    @Query("SELECT DISTINCT b FROM Book b")
    List<Book> findAllWithDetails();

    @EntityGraph(attributePaths = {"authors", "category", "publisher"})
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") UUID id);
}
