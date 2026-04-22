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
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.search.dtos.ItemSearchDto;
import com.ozz.atlas.supply.item.search.service.ItemSearchService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/items")
public class SupplyItemController {

    private final SupplyItemService supplyItemService;
    private final ItemSearchService itemSearchService;

    @PostMapping
    public ResponseEntity<?> createItem(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @Valid @RequestBody CreateItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyItemService.createItem(organizationPublicId, organizationType, request));
    }

    @PutMapping("/{itemPublicId}")
    public ResponseEntity<?> updateItem(
            @PathVariable String itemPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @Valid @RequestBody UpdateItemRequest request
    ) {
        return ResponseEntity.ok(
                supplyItemService.updateItem(
                        organizationPublicId,
                        organizationType,
                        itemPublicId,
                        request
                )
        );
    }

    @DeleteMapping("/{itemPublicId}")
    public ResponseEntity<?> deleteItem(
            @PathVariable String itemPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        supplyItemService.deleteItem(organizationPublicId, organizationType, itemPublicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{itemPublicId}")
    public ResponseEntity<?> getItem(@PathVariable String itemPublicId) {
        return ResponseEntity.ok(supplyItemService.getItem(itemPublicId));
    }


    @GetMapping
    public ResponseEntity<?> getItemList(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "supplierPublicId", required = false) String supplierPublicId,
            @RequestParam(value = "supplierOrganizationPublicId", required = false) String supplierOrganizationPublicId,
            @RequestParam(value = "itemCategoryPublicId", required = false) String itemCategoryPublicId,
            @RequestParam(value = "status", required = false) Status status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        supplyItemService.validateItemListAccess(
                organizationPublicId,
                organizationType,
                supplierPublicId,
                supplierOrganizationPublicId
        );

        ItemSearchDto searchDto = ItemSearchDto.builder()
                .keyword(keyword)
                .supplierPublicId(supplierPublicId)
                .supplierOrganizationPublicId(supplierOrganizationPublicId)
                .itemCategoryPublicId(itemCategoryPublicId)
                .status(status)
                .build();

        if (itemSearchService.hasSearchCondition(searchDto)) {
            return ResponseEntity.ok(itemSearchService.search(pageable, searchDto));
        }

        return ResponseEntity.ok(supplyItemService.getItemList(pageable));
    }
}
