package com.bookstore.books.service;

import com.bookstore.books.category.dto.CategoryRequest;
import com.bookstore.books.category.dto.CategoryResponse;
import com.bookstore.books.category.entity.Category;
import com.bookstore.books.category.repository.CategoryRepository;
import com.bookstore.books.category.service.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void GivenValidRequest_WhenCreateCategory_ThenPersistAndReturnResponse() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Fiction")
                .description("Stories")
                .build();
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(UUID.randomUUID());
            return category;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.getName()).isEqualTo("Fiction");
        assertThat(response.getId()).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void GivenExistingCategory_WhenUpdateCategory_ThenApplyChanges() {
        UUID id = UUID.randomUUID();
        Category existing = Category.builder().id(id).name("Old").description("d").build();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));

        CategoryResponse response = categoryService.updateCategory(
                CategoryRequest.builder().name("New").description("updated").build(), id);

        assertThat(response.getName()).isEqualTo("New");
        assertThat(response.getDescription()).isEqualTo("updated");
    }

    @Test
    void GivenUnknownId_WhenUpdateCategory_ThenThrowRuntimeException() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(
                CategoryRequest.builder().name("X").build(), id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");
    }

    @Test
    void GivenExistingCategory_WhenGetById_ThenReturnResponse() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(
                Category.builder().id(id).name("Fiction").build()));

        CategoryResponse response = categoryService.getById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Fiction");
    }

    @Test
    void GivenUnknownId_WhenGetById_ThenThrowRuntimeException() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");
    }

    @Test
    void GivenCategoryId_WhenDeleteById_ThenDelegateToRepository() {
        UUID id = UUID.randomUUID();
        categoryService.deleteById(id);
        verify(categoryRepository).deleteById(id);
    }

    @Test
    void GivenCategoriesExist_WhenGetAllCategories_ThenReturnMappedList() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                Category.builder().id(UUID.randomUUID()).name("Fiction").build()));

        assertThat(categoryService.getAllCategories()).hasSize(1);
    }
}
