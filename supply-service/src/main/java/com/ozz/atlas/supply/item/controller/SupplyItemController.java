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

    @PutMapping("/{itemId}")
    public ItemResponse updateItem(@PathVariable Long itemId,
                           @Valid @RequestBody UpdateItemRequest request) {

        return supplyItemService.updateItem(itemId, request);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        supplyItemService.deleteItem(itemId);
    }

    @GetMapping("/{itemId}")
    public ItemResponse getItem(@PathVariable Long itemId) {
        return supplyItemService.getItem(itemId);
    }

    @GetMapping
    public Page<ItemResponse> getItemList(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return supplyItemService.getItemList(pageable);
    }



























}
