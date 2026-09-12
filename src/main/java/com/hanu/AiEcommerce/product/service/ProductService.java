package com.hanu.AiEcommerce.product.service;

import com.hanu.AiEcommerce.product.dto.CreateProductRequest;
import com.hanu.AiEcommerce.product.dto.ProductFilterRequest;
import com.hanu.AiEcommerce.product.dto.ProductResponse;
import com.hanu.AiEcommerce.product.entity.Category;
import com.hanu.AiEcommerce.product.entity.Product;
import com.hanu.AiEcommerce.product.repository.CategoryRepository;
import com.hanu.AiEcommerce.product.repository.ProductRepository;
import com.hanu.AiEcommerce.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        if(productRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("Product already exists by sku" + request.sku());
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with category Id" + request.categoryId()));

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .sku(request.sku())
                .price(request.price())
                .category(category)
                .sellerId(request.sellerId())
                .build();

        product = productRepository.save(product);

        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id" + id));

        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(
            Pageable pageable,
            ProductFilterRequest filterRequest
    ) {

        Specification<Product> specification =
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if(filterRequest.categoryId() != null) {
            specification = ProductSpecification.hasCategoryId(
                    filterRequest.categoryId()
            );
        }

        if(filterRequest.sellerId() != null) {
            specification = ProductSpecification.hasSellerId(
                    filterRequest.sellerId()
            );
        }

        if(filterRequest.minPrice() != null) {
            specification = ProductSpecification.priceGreaterThanOrEqualTo(
                    filterRequest.minPrice()
            );
        }

        if(filterRequest.maxPrice() != null) {
            specification = ProductSpecification.priceLessThanOrEqualTo(
                    filterRequest.maxPrice()
            );
        }

        return productRepository.findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    //=============== helper ================

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .createdAt(product.getCreatedAt())
                .sellerId(product.getSellerId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .price(product.getPrice())
                .build();
    }
}
