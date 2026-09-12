package com.hanu.AiEcommerce.product.specification;

import com.hanu.AiEcommerce.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecification {

    public ProductSpecification() {}

    public static Specification<Product> hasCategoryId(Long categoryId) {

        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(
                    root.get("category").get("id"),
                    categoryId
            );
    }

    public static Specification<Product> hasSellerId(Long sellerId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("sellerId"),
                        sellerId
                );
    }

    public static Specification<Product> priceGreaterThanOrEqualTo(
            java.math.BigDecimal minPrice
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        minPrice
                );
    }

    public static Specification<Product> priceLessThanOrEqualTo(
            java.math.BigDecimal maxPrice
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        maxPrice
                );
    }
}
