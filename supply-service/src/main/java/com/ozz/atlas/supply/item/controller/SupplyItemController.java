package com.ozz.atlas.supply.item.controller;

import com.ozz.atlas.supply.item.dtos.CreateItemRequest;
import com.ozz.atlas.supply.item.dtos.UpdateItemRequest;
import com.ozz.atlas.supply.item.service.SupplyItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/items")
public class SupplyItemController {

    private final SupplyItemService supplyItemService;

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody CreateItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyItemService.createItem(request));
    }

    @PutMapping("/{itemPublicId}")
    public ResponseEntity<?> updateItem(
            @PathVariable String itemPublicId,
            @Valid @RequestBody UpdateItemRequest request
    ) {
        return ResponseEntity.ok(supplyItemService.updateItem(itemPublicId, request));
    }

    @DeleteMapping("/{itemPublicId}")
    public ResponseEntity<?> deleteItem(@PathVariable String itemPublicId) {
        supplyItemService.deleteItem(itemPublicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{itemPublicId}")
    public ResponseEntity<?> getItem(@PathVariable String itemPublicId) {
        return ResponseEntity.ok(supplyItemService.getItem(itemPublicId));
    }

    @GetMapping
    public ResponseEntity<?> getItemList(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(supplyItemService.getItemList(pageable));
    }
}
