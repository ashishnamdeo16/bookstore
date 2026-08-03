package com.bookstore.books.category.mapper;

import com.bookstore.books.category.dto.CategoryResponse;
import com.bookstore.books.category.entity.Category;

public class CategoryMapper {

    private CategoryMapper() {
        /* This utility class should not be instantiated */
    }

    public static CategoryResponse toResponse(Category category){

        return CategoryResponse
                .builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
