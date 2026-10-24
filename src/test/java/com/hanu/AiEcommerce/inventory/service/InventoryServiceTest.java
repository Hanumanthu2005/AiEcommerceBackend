package com.hanu.AiEcommerce.inventory.service;

import com.hanu.AiEcommerce.common.exception.DuplicateResourceException;
import com.hanu.AiEcommerce.common.exception.InsufficientStockException;
import com.hanu.AiEcommerce.common.exception.InvalidStockAdjustmentException;
import com.hanu.AiEcommerce.common.exception.InvalidStockReleaseException;
import com.hanu.AiEcommerce.common.exception.ResourceNotFoundException;
import com.hanu.AiEcommerce.inventory.dto.InventoryResponse;
import com.hanu.AiEcommerce.inventory.entity.Inventory;
import com.hanu.AiEcommerce.inventory.repository.InventoryRepository;
import com.hanu.AiEcommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = Inventory.builder()
                .id(20L)
                .productId(100L)
                .quantity(10)
                .reservedQuantity(2)
                .version(0L)
                .build();
    }

    @Test
    void reserveStock_increasesReservedAndUpdatesAvailableQuantity() {
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.reserveStock(100L, 3);

        assertThat(response.quantity()).isEqualTo(10);
        assertThat(response.reservedQuantity()).isEqualTo(5);
        assertThat(response.availableQuantity()).isEqualTo(5);
    }

    @Test
    void reserveStock_throwsWhenQuantityIsNotPositive() {
        assertThatThrownBy(() -> inventoryService.reserveStock(100L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        verify(inventoryRepository, never()).findByProductId(any());
    }

    @Test
    void reserveStock_throwsWhenAvailableStockIsInsufficient() {
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.reserveStock(100L, 9))
                .isInstanceOf(InsufficientStockException.class);

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void releaseStock_decreasesReservedQuantity() {
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.releaseStock(100L, 1);

        assertThat(response.reservedQuantity()).isEqualTo(1);
        assertThat(response.availableQuantity()).isEqualTo(9);
    }

    @Test
    void releaseStock_rejectsReleasingMoreThanReserved() {
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.releaseStock(100L, 3))
                .isInstanceOf(InvalidStockReleaseException.class);

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void adjustStock_rejectsQuantityBelowReservedStock() {
        when(productRepository.existsByIdAndSellerId(100L, 12L)).thenReturn(true);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.adjustStock(100L, -9, 12L, false))
                .isInstanceOf(InvalidStockAdjustmentException.class);

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createInventory_rejectsDuplicateInventory() {
        when(productRepository.existsByIdAndSellerId(100L, 12L)).thenReturn(true);
        when(inventoryRepository.existsByProductId(100L)).thenReturn(true);

        assertThatThrownBy(() -> inventoryService.createInventory(
                new com.hanu.AiEcommerce.inventory.dto.CreateInventoryRequest(100L, 10),
                12L,
                false
        )).isInstanceOf(DuplicateResourceException.class);

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createInventory_rejectsSellerWhoDoesNotOwnProduct() {
        when(productRepository.existsByIdAndSellerId(100L, 12L)).thenReturn(false);

        assertThatThrownBy(() -> inventoryService.createInventory(
                new com.hanu.AiEcommerce.inventory.dto.CreateInventoryRequest(100L, 10),
                12L,
                false
        )).isInstanceOf(AccessDeniedException.class);

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void reserveStock_throwsWhenInventoryDoesNotExist() {
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.reserveStock(100L, 1))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
