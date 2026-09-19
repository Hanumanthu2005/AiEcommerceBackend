package com.hanu.AiEcommerce.inventory.controller;

import com.hanu.AiEcommerce.inventory.dto.*;
import com.hanu.AiEcommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid
            @RequestBody
            CreateInventoryRequest request
    ) {

        InventoryResponse response = inventoryService.createInventory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable Long productId) {

        return ResponseEntity
                .ok(inventoryService.getInventoryByProductId(productId));
    }

    @PatchMapping("/product/{productId}/stock")
    public ResponseEntity<InventoryResponse> adjustStock(
            @PathVariable
            Long productId,

            @Valid
            @RequestBody
            AdjustStockRequest request
    ) {
        return ResponseEntity.ok(
                inventoryService.adjustStock(productId, request.quantity())
        );
    }

    @PatchMapping("/product/{productId}/reserve")
    public ResponseEntity<InventoryResponse> reserveStock(
            @PathVariable
            Long productId,

            @Valid
            @RequestBody
            ReserveStockRequest request
    ) {

        return ResponseEntity.ok(
                inventoryService.reserveStock(productId, request.quantity())
        );
    }

    @PatchMapping("/product/{productId}/release")
    public ResponseEntity<InventoryResponse> releaseStock(
            @PathVariable
            Long productId,

            @Valid
            @RequestBody
            ReleaseStockRequest request
    ) {

        return ResponseEntity.ok(
                inventoryService.releaseStock(productId, request.quantity())
        );
    }
}
