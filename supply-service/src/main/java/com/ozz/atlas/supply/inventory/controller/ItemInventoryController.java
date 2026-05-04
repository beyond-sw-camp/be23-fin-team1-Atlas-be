package com.ozz.atlas.supply.inventory.controller;

import com.ozz.atlas.supply.inventory.dtos.CreateItemInventoryRequest;
import com.ozz.atlas.supply.inventory.dtos.UpdateItemInventoryRequest;
import com.ozz.atlas.supply.inventory.service.ItemInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supply/inventories")
@RequiredArgsConstructor
public class ItemInventoryController {

    private final ItemInventoryService itemInventoryService;

    @PostMapping
    public ResponseEntity<?> createInventory(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @Valid @RequestBody CreateItemInventoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemInventoryService.createInventory(organizationPublicId, organizationType, request));
    }

    @GetMapping
    public ResponseEntity<?> getInventories(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(itemInventoryService.getInventories(organizationPublicId, organizationType));
    }

    @GetMapping("/{inventoryPublicId}")
    public ResponseEntity<?> getInventory(
            @PathVariable String inventoryPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(
                itemInventoryService.getInventory(
                        organizationPublicId,
                        organizationType,
                        inventoryPublicId
                )
        );
    }

    @PutMapping("/{inventoryPublicId}")
    public ResponseEntity<?> updateInventory(
            @PathVariable String inventoryPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @Valid @RequestBody UpdateItemInventoryRequest request
    ) {
        return ResponseEntity.ok(
                itemInventoryService.updateInventory(
                        organizationPublicId,
                        organizationType,
                        inventoryPublicId,
                        request
                )
        );
    }

    @DeleteMapping("/{inventoryPublicId}")
    public ResponseEntity<?> deleteInventory(
            @PathVariable String inventoryPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        itemInventoryService.deleteInventory(organizationPublicId, organizationType, inventoryPublicId);
        return ResponseEntity.noContent().build();
    }
}
