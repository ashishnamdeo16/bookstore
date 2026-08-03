package com.bookstore.books.publisher.mapper;

import com.bookstore.books.publisher.dto.PublisherResponse;
import com.bookstore.books.publisher.entity.Publisher;

public class PublisherMapper {

    private PublisherMapper() {
        /* This utility class should not be instantiated */
    }

    public static PublisherResponse toResponse(Publisher publisher){

        return PublisherResponse
                .builder()
                .id(publisher.getId())
                .name(publisher.getName())
                .address(publisher.getAddress())
                .build();
    }
}
