package com.bookstore.books.publisher.service;

import com.bookstore.books.category.dto.CategoryRequest;
import com.bookstore.books.category.dto.CategoryResponse;
import com.bookstore.books.publisher.dto.PublisherRequest;
import com.bookstore.books.publisher.dto.PublisherResponse;

import java.util.List;
import java.util.UUID;

public interface PublisherService {

    PublisherResponse createPublisher(PublisherRequest request);

    PublisherResponse updatePublisher(PublisherRequest request, UUID id);

    PublisherResponse getPublisherById(UUID id);

    void deletePublisherById(UUID id);

    List<PublisherResponse> getAllPublishers();

}
