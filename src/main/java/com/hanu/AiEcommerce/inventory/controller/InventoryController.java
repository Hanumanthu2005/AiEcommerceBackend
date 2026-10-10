
package com.hanu.AiEcommerce.inventory.controller;

import com.hanu.AiEcommerce.inventory.dto.*;
import com.hanu.AiEcommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    private Long getUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            Authentication authentication,
            @Valid @RequestBody CreateInventoryRequest request
    ) {
        InventoryResponse response = inventoryService.createInventory(
                request,
                getUserId(authentication),
                isAdmin(authentication)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                inventoryService.getInventoryByProductId(
                        productId,
                        getUserId(authentication),
                        isAdmin(authentication)
                )
        );
    }

    @PatchMapping("/product/{productId}/stock")
    public ResponseEntity<InventoryResponse> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody AdjustStockRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                inventoryService.adjustStock(
                        productId,
                        request.quantity(),
                        getUserId(authentication),
                        isAdmin(authentication)
                )
        );
    }

}


