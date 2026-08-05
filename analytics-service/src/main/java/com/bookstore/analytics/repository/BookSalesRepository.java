package com.bookstore.analytics.repository;

import com.bookstore.analytics.entity.BookSales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookSalesRepository extends JpaRepository<BookSales, UUID> {

    List<BookSales> findTop10ByOrderByQuantitySoldDesc();
}
