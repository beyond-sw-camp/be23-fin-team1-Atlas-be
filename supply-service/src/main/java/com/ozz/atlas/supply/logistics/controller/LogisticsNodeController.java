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
@RequestMapping("/api/logistics-nodes")
public class LogisticsNodeController {

    private final LogisticsNodeService logisticsNodeService;

    public LogisticsNodeController(LogisticsNodeService logisticsNodeService) {
        this.logisticsNodeService = logisticsNodeService;
    }

//    창고 생성
    @PostMapping
    public ResponseEntity<LogisticsNodeResponseDto> createLogisticsNode(@Valid @RequestBody CreateLogisticsNodeRequestDto dto){
        LogisticsNodeResponseDto createNode = logisticsNodeService.createLogisticsNode(dto);

        return ResponseEntity.created(URI.create("/api/logistics-nodes/" + createNode.getPublicId())).body(createNode);
    }

//    창고 목록 조회
    @GetMapping
    public Page<LogisticsNodeResponseDto> getLogisticsNodes(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)Pageable pageable){
        return logisticsNodeService.getLogisticsNodes(pageable);
    }

//    창고 상세 조회
    @GetMapping("/{publicId}")
    public ResponseEntity<LogisticsNodeResponseDto> getLogisticsNode(@PathVariable String publicId){
        return ResponseEntity.ok(logisticsNodeService.getLogisticsNode(publicId));
    }

//    창고 수정
    @PatchMapping("/{publicId}")
    public ResponseEntity<LogisticsNodeResponseDto> updateLogisticsNode(
            @PathVariable String publicId, @Valid @RequestBody UpdateLogisticsNodeRequestDto dto){
        return ResponseEntity.ok(logisticsNodeService.updateLogisticsNode(publicId, dto));
    }

//    창고 활성화
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<LogisticsNodeResponseDto> activateLogisticsNode(@PathVariable String publicId){
        return ResponseEntity.ok(logisticsNodeService.activateLogisticsNode(publicId));
    }

//    창고 비활성화
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<LogisticsNodeResponseDto> deactivateLogisticsNode(@PathVariable String publicId){
        return ResponseEntity.ok(logisticsNodeService.deactivateLogisticsNode(publicId));
    }
}
