
package com.hanu.AiEcommerce.inventory.service;

import com.hanu.AiEcommerce.common.exception.*;
import com.hanu.AiEcommerce.inventory.dto.*;
import com.hanu.AiEcommerce.inventory.entity.Inventory;
import com.hanu.AiEcommerce.inventory.repository.InventoryRepository;
import com.hanu.AiEcommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    private void checkProductAccess(
            Long productId,
            Long userId,
            boolean isAdmin
    ) {
        if (isAdmin) {
            if (!productRepository.existsById(productId)) {
                throw new ResourceNotFoundException(
                        "Product not found with id " + productId
                );
            }
            return;
        }

        boolean ownsProduct = productRepository
                .existsByIdAndSellerId(productId, userId);

        if (!ownsProduct) {
            throw new AccessDeniedException(
                    "You are not authorized to manage this product's inventory"
            );
        }
    }

    @Transactional
    public InventoryResponse createInventory(
            CreateInventoryRequest request,
            Long userId,
            boolean isAdmin
    ) {
        checkProductAccess(request.productId(), userId, isAdmin);

        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new DuplicateResourceException(
                    "Inventory already exists for product id "
                            + request.productId()
            );
        }

        Inventory inventory = Inventory.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .reservedQuantity(0)
                .build();

        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(
            Long productId,
            Long userId,
            boolean isAdmin
    ) {
        checkProductAccess(productId, userId, isAdmin);

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product id " + productId
                ));

        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryResponse adjustStock(
            Long productId,
            Integer quantity,
            Long userId,
            boolean isAdmin
    ) {
        checkProductAccess(productId, userId, isAdmin);

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product id " + productId
                ));

        int newQuantity = Math.addExact(
                inventory.getQuantity(), quantity
        );

        if (newQuantity < inventory.getReservedQuantity()) {
            throw new InvalidStockAdjustmentException(
                    "Stock cannot be reduced below reserved stock"
            );
        }

        if (newQuantity < 0) {
            throw new InvalidStockAdjustmentException(
                    "Stock quantity cannot be negative"
            );
        }

        inventory.setQuantity(newQuantity);

        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryResponse reserveStock(
            Long productId,
            Integer quantity
    ) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Reservation quantity must be positive"
            );
        }

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product id " + productId
                ));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product id " + productId
            );
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + quantity
        );

        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryResponse releaseStock(
            Long productId,
            Integer quantity
    ) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Release quantity must be positive"
            );
        }

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product id " + productId
                ));

        if (quantity > inventory.getReservedQuantity()) {
            throw new InvalidStockReleaseException(
                    "Cannot release more stock than currently reserved"
            );
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity
        );

        return mapToResponse(inventoryRepository.save(inventory));
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getVersion(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}
