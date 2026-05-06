package com.ozz.atlas.supply.inventory.controller;

import com.ozz.atlas.supply.inventory.dtos.CreateItemInventoryRequest;
import com.ozz.atlas.supply.inventory.dtos.UpdateItemInventoryRequest;
import com.ozz.atlas.supply.inventory.service.ItemInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supply/inventories")
@RequiredArgsConstructor
@Tag(name = "ItemInventory", description = "품목 재고 생성, 조회, 수정, 삭제 API")
public class ItemInventoryController {

    private final ItemInventoryService itemInventoryService;

    @PostMapping
    @Operation(summary = "재고 생성", description = "조직 권한 범위 내에서 품목 재고를 생성한다.")
    public ResponseEntity<?> createInventory(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @Valid @RequestBody CreateItemInventoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemInventoryService.createInventory(organizationPublicId, organizationType, request));
    }

    @GetMapping
    @Operation(summary = "재고 목록 조회", description = "조직 권한 범위 내에서 전체 재고 목록을 조회한다.")
    public ResponseEntity<?> getInventories(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(itemInventoryService.getInventories(organizationPublicId, organizationType));
    }

    @GetMapping("/{inventoryPublicId}")
    @Operation(summary = "재고 상세 조회", description = "재고 공개 ID로 재고 상세 정보를 조회한다.")
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

    @GetMapping("/nodes/{nodePublicId}")
    @Operation(summary = "거점별 재고 조회", description = "물류 거점 공개 ID로 해당 거점의 재고 목록을 조회한다.")
    public ResponseEntity<?> getNodeInventories(
            @PathVariable String nodePublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(
                itemInventoryService.getNodeInventories(
                        organizationPublicId,
                        organizationType,
                        nodePublicId
                )
        );
    }

    @GetMapping("/nodes/{nodePublicId}/recent")
    @Operation(summary = "거점 최근 재고 조회", description = "물류 거점 공개 ID로 해당 거점의 최근 재고 목록을 조회한다.")
    public ResponseEntity<?> getRecentNodeInventories(
            @PathVariable String nodePublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(
                itemInventoryService.getRecentNodeInventories(
                        organizationPublicId,
                        organizationType,
                        nodePublicId
                )
        );
    }

    @PutMapping("/{inventoryPublicId}")
    @Operation(summary = "재고 수정", description = "재고 공개 ID로 재고 정보를 수정한다.")
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
    @Operation(summary = "재고 삭제", description = "재고 공개 ID로 재고를 삭제한다.")
    public ResponseEntity<?> deleteInventory(
            @PathVariable String inventoryPublicId,
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        itemInventoryService.deleteInventory(organizationPublicId, organizationType, inventoryPublicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "재고 요약 조회", description = "조직 권한 범위 내 재고 현황 요약 정보를 조회한다.")
    public ResponseEntity<?> getInventorySummary(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType
    ) {
        return ResponseEntity.ok(
                itemInventoryService.getInventorySummary(organizationPublicId, organizationType)
        );
    }

    @GetMapping("/items/{itemPublicId}")
    @Operation(summary = "품목별 재고 조회", description = "품목 공개 ID로 해당 품목의 재고 목록을 조회한다.")
    public ResponseEntity<?> getItemInventories(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @RequestHeader("X-Organization-Type") String organizationType,
            @PathVariable String itemPublicId
    ) {
        return ResponseEntity.ok(
                itemInventoryService.getItemInventories(organizationPublicId, organizationType, itemPublicId)
        );
    }

}
