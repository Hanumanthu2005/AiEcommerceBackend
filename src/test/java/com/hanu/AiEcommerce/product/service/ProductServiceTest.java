package com.hanu.AiEcommerce.product.service;

import com.hanu.AiEcommerce.common.exception.DuplicateResourceException;
import com.hanu.AiEcommerce.common.exception.ResourceNotFoundException;
import com.hanu.AiEcommerce.product.dto.CreateProductRequest;
import com.hanu.AiEcommerce.product.dto.ProductResponse;
import com.hanu.AiEcommerce.product.entity.Category;
import com.hanu.AiEcommerce.product.entity.Product;
import com.hanu.AiEcommerce.product.repository.CategoryRepository;
import com.hanu.AiEcommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_savesProductAndReturnsResponse() {
        Category category = Category.builder()
                .id(7L)
                .name("Electronics")
                .description("Electronic products")
                .build();

        CreateProductRequest request = new CreateProductRequest(
                "Wireless Mouse",
                "Bluetooth wireless mouse",
                "MOUSE-001",
                new BigDecimal("799.00"),
                12L,
                7L
        );

        when(productRepository.existsBySku("MOUSE-001")).thenReturn(false);
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(101L);
            return product;
        });

        ProductResponse response = productService.createProduct(request);

        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.name()).isEqualTo("Wireless Mouse");
        assertThat(response.sku()).isEqualTo("MOUSE-001");
        assertThat(response.price()).isEqualByComparingTo("799.00");
        assertThat(response.sellerId()).isEqualTo(12L);
        assertThat(response.categoryId()).isEqualTo(7L);
        assertThat(response.categoryName()).isEqualTo("Electronics");

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_throwsWhenSkuAlreadyExists() {
        CreateProductRequest request = new CreateProductRequest(
                "Wireless Mouse",
                "Bluetooth wireless mouse",
                "MOUSE-001",
                new BigDecimal("799.00"),
                12L,
                7L
        );

        when(productRepository.existsBySku("MOUSE-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("MOUSE-001");

        verify(categoryRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_throwsWhenCategoryDoesNotExist() {
        CreateProductRequest request = new CreateProductRequest(
                "Wireless Mouse",
                "Bluetooth wireless mouse",
                "MOUSE-001",
                new BigDecimal("799.00"),
                12L,
                7L
        );

        when(productRepository.existsBySku("MOUSE-001")).thenReturn(false);
        when(categoryRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("7");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_throwsWhenProductDoesNotExist() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }
}
