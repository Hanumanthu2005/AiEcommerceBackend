package com.hanu.AiEcommerce.product.service;

import com.hanu.AiEcommerce.common.exception.DuplicateResourceException;
import com.hanu.AiEcommerce.product.dto.CategoryResponse;
import com.hanu.AiEcommerce.product.dto.CreateCategoryRequest;
import com.hanu.AiEcommerce.product.entity.Category;
import com.hanu.AiEcommerce.product.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_savesAndReturnsCategoryResponse() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                "Accessories",
                "Computer accessories"
        );

        when(categoryRepository.existsByNameIgnoreCase("Accessories")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(5L);
            return category;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.name()).isEqualTo("Accessories");
        assertThat(response.description()).isEqualTo("Computer accessories");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_throwsWhenNameAlreadyExistsIgnoringCase() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                "Accessories",
                "Computer accessories"
        );

        when(categoryRepository.existsByNameIgnoreCase("Accessories")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Accessories");

        verify(categoryRepository, never()).save(any());
    }
}
