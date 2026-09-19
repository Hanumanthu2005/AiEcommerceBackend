package com.hanu.AiEcommerce.inventory.service;

import com.hanu.AiEcommerce.common.exception.*;
import com.hanu.AiEcommerce.inventory.dto.*;
import com.hanu.AiEcommerce.inventory.entity.Inventory;
import com.hanu.AiEcommerce.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {

        if(inventoryRepository.existsByProductId(request.productId())) {
            throw new DuplicateResourceException(
                    "Inventory already exist with product id " + request.productId()
            );
        }

        Inventory inventory = Inventory.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .reservedQuantity(0)
                .build();

        inventory = inventoryRepository.save(inventory);

        return mapToResponse(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow( () -> new ResourceNotFoundException(
                        "Inventory not found with id " + productId
                        )
                );

        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryResponse adjustStock(
            Long productId,
            Integer quantity
    ) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with id " + productId
                ));

        int newQuantity = inventory.getQuantity() + quantity;

        if(newQuantity < inventory.getReservedQuantity()) {
            throw new InvalidStockAdjustmentException("Stock cannot be reduced below reserved stock");
        }

        if(newQuantity < 0) {
            throw new InvalidStockAdjustmentException("Stock quantity cannot be negative");
        }

        inventory.setQuantity(newQuantity);

        inventory = inventoryRepository.save(inventory);

        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryResponse reserveStock(
            Long productId,
            Integer quantity
    ) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with id " + productId
                ));

        if(inventory.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product with id " + productId
                    + ", Available stock " + inventory.getAvailableQuantity()
                    + ", Request stock " + quantity
            );
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + quantity
        );

        inventory = inventoryRepository.save(inventory);

        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryResponse releaseStock(
            Long productId,
            Integer quantity
    ) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with product id " + productId
                ));

        if(quantity > inventory.getReservedQuantity()) {
            throw new InvalidStockReleaseException("Cannot release more stock than currently reserved");
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity
        );

        inventory = inventoryRepository.save(inventory);

        return mapToResponse(inventory);
    }


    // ===============   HELPER ==================

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
