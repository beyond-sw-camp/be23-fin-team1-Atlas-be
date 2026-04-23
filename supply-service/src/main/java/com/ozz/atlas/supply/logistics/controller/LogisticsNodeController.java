package com.ozz.atlas.supply.logistics.controller;

import com.ozz.atlas.supply.logistics.dtos.CreateLogisticsNodeRequestDto;
import com.ozz.atlas.supply.logistics.dtos.LogisticsNodeResponseDto;
import com.ozz.atlas.supply.logistics.dtos.UpdateLogisticsNodeRequestDto;
import com.ozz.atlas.supply.logistics.service.LogisticsNodeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/supply/logistics-nodes")
public class LogisticsNodeController {

    private final LogisticsNodeService logisticsNodeService;

    public LogisticsNodeController(LogisticsNodeService logisticsNodeService) {
        this.logisticsNodeService = logisticsNodeService;
    }

    // 물류거점 생성
    @PostMapping
    public ResponseEntity<LogisticsNodeResponseDto> createLogisticsNode(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateLogisticsNodeRequestDto dto
    ) {
        LogisticsNodeResponseDto createNode = logisticsNodeService.createLogisticsNode(
                organizationPublicId,
                organizationType,
                userRole,
                dto
        );

        return ResponseEntity
                .created(URI.create("/api/supply/logistics-nodes/" + createNode.getPublicId()))
                .body(createNode);
    }

    // 물류거점 목록 조회
    @GetMapping
    public Page<LogisticsNodeResponseDto> getLogisticsNodes(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return logisticsNodeService.getLogisticsNodes(
                organizationPublicId,
                organizationType,
                userRole,
                pageable
        );
    }

    // 물류거점 상세 조회
    @GetMapping("/{publicId}")
    public ResponseEntity<LogisticsNodeResponseDto> getLogisticsNode(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                logisticsNodeService.getLogisticsNode(
                        organizationPublicId,
                        organizationType,
                        userRole,
                        publicId
                )
        );
    }

    // 물류거점 수정
    @PatchMapping("/{publicId}")
    public ResponseEntity<LogisticsNodeResponseDto> updateLogisticsNode(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId,
            @Valid @RequestBody UpdateLogisticsNodeRequestDto dto
    ) {
        return ResponseEntity.ok(
                logisticsNodeService.updateLogisticsNode(
                        organizationPublicId,
                        organizationType,
                        userRole,
                        publicId,
                        dto
                )
        );
    }

    // 물류거점 활성화
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<LogisticsNodeResponseDto> activateLogisticsNode(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                logisticsNodeService.activateLogisticsNode(
                        organizationPublicId,
                        organizationType,
                        userRole,
                        publicId
                )
        );
    }

    // 물류거점 비활성화
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<LogisticsNodeResponseDto> deactivateLogisticsNode(
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                logisticsNodeService.deactivateLogisticsNode(
                        organizationPublicId,
                        organizationType,
                        userRole,
                        publicId
                )
        );
    }
}
