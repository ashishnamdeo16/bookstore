package com.bookstore.books.category.service;

import com.bookstore.books.category.dto.CategoryRequest;
import com.bookstore.books.category.dto.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(CategoryRequest request,UUID id);

    CategoryResponse getById(UUID id);

    void deleteById(UUID id);

    List<CategoryResponse> getAllCategories();

}
