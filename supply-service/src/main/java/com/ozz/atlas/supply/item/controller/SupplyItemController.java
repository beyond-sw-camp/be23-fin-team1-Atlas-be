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
@RequestMapping("/item")
public class SupplyItemController {

    private final SupplyItemService supplyItemService;

//    품목 등록
    @PostMapping("/create")
    public ResponseEntity<?> createItem(@Valid @RequestBody CreateItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyItemService.createItem(request));
    }

//    품목 수정
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(@PathVariable Long itemId,
                           @Valid @RequestBody UpdateItemRequest request) {

        return ResponseEntity.ok(supplyItemService.updateItem(itemId, request));
    }

//    품목 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId) {
        supplyItemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

//    품목 단건 조회
    @GetMapping("/{itemId}")
    public ResponseEntity<?> getItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(supplyItemService.getItem(itemId));
    }

//    품목 목록 조회
    @GetMapping
    public ResponseEntity<?>getItemList(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(supplyItemService.getItemList(pageable));
    }



























}
