package com.hanu.AiEcommerce.product.controller;

import com.hanu.AiEcommerce.product.dto.CreateProductRequest;
import com.hanu.AiEcommerce.product.dto.ProductFilterRequest;
import com.hanu.AiEcommerce.product.dto.ProductResponse;
import com.hanu.AiEcommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid
            @RequestBody
            CreateProductRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable
            Long id
    ) {
        return ResponseEntity.ok(
                productService.getProductById(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,

            Pageable pageable
    ) {

        ProductFilterRequest request = new ProductFilterRequest(
                categoryId,
                sellerId,
                minPrice,
                maxPrice
        );

        return ResponseEntity.ok(
                productService.getAllProducts(pageable, request)
        );
    }
}
