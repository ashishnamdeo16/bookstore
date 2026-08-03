package com.bookstore.books.publisher.repository;

import com.bookstore.books.publisher.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface PublisherRepository extends JpaRepository<Publisher, UUID> {

}
