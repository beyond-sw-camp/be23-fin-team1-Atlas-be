package com.ozz.atlas.supply.item.controller;

import com.ozz.atlas.supply.item.dtos.CreateItemRequest;
import com.ozz.atlas.supply.item.dtos.ItemResponse;
import com.ozz.atlas.supply.item.dtos.UpdateItemRequest;
import com.ozz.atlas.supply.item.service.SupplyItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/item")
public class SupplyItemController {

    private final SupplyItemService supplyItemService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse createItem(@Valid @RequestBody CreateItemRequest request) {
        return supplyItemService.createItem(request);
    }

    @PutMapping("/{publicId}")
    public ItemResponse updateItem(@PathVariable String publicId,
                           @Valid @RequestBody UpdateItemRequest request) {

        return supplyItemService.updateItem(publicId, request);
    }

    @DeleteMapping("/{publicId}")
    public void deleteItem(@PathVariable String publicId) {
        supplyItemService.deleteItem(publicId);
    }

    @GetMapping("/{publicId}")
    public ItemResponse getItem(@PathVariable String publicId) {
        return supplyItemService.getItem(publicId);
    }

    @GetMapping
    public Page<ItemResponse> getItems(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return supplyItemService.getItems(pageable);
    }



























}
