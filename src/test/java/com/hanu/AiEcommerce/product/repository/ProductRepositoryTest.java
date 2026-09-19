package com.hanu.AiEcommerce.product.repository;

import com.hanu.AiEcommerce.product.entity.Category;
import com.hanu.AiEcommerce.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import static org.assertj.core.api.Assertions.assertThat;



@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveAndFindProduct() {

        Category category = Category.builder()
                .name("Test Electronics")
                .description("Test Description")
                .build();

        category = categoryRepository.save(category);

        Product product = Product.builder()
                .name("Test product")
                .sku("test-product-001")
                .description("test description")
                .price(new BigDecimal("500000.00"))
                .sellerId(1l)
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);

        assertThat(savedProduct.getId()).isNotNull();

        Product fourndProduct = productRepository.findById(savedProduct.getId())
                .orElseThrow();

        assertThat(fourndProduct.getName())
                .isEqualTo("Test product");

        assertThat(fourndProduct.getSku())
                .isEqualTo("test-product-001");

        assertThat(fourndProduct.getPrice())
                .isEqualByComparingTo("500000.00");

        assertThat(fourndProduct.getCategory().getName())
                .isEqualTo("Test Electronics");
    }

    @Test
    void shouldFindProductBySku() {

        Category category = Category.builder()
                .name("Test Category SKU")
                .description("Test category")
                .build();

        category = categoryRepository.save(category);

        Product product = Product.builder()
                .name("Test Mouse")
                .description("Test mouse")
                .sku("TEST-MOUSE-001")
                .price(new BigDecimal("2500.00"))
                .sellerId(1L)
                .category(category)
                .build();

        productRepository.save(product);

        var result = productRepository.findBySku("TEST-MOUSE-001");

        assertThat(result)
                .isPresent();

        assertThat(result.get().getName())
                .isEqualTo("Test Mouse");
    }
}
