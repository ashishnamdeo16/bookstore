package com.bookstore.books.category.controller;

import com.bookstore.books.category.dto.CategoryRequest;
import com.bookstore.books.category.dto.CategoryResponse;
import com.bookstore.books.category.service.CategoryServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryServiceImpl categoryService;

    public CategoryController(CategoryServiceImpl categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request){
        return new ResponseEntity<>(categoryService.createCategory(request), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@RequestBody CategoryRequest request, @PathVariable UUID id){
        return new ResponseEntity<>(categoryService.updateCategory(request,id), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id){
        return new ResponseEntity<>(categoryService.getById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategoryById(@PathVariable UUID id){
        categoryService.deleteById(id);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(){
        return new ResponseEntity<>(categoryService.getAllCategories(), HttpStatus.OK);
    }

}

