package com.ozz.atlas.supply.logistics.controller;

import com.ozz.atlas.supply.inventory.dtos.ItemInventoryResponse;
import com.ozz.atlas.supply.inventory.service.ItemInventoryService;
import com.ozz.atlas.supply.logistics.dtos.CreateLogisticsNodeRequestDto;
import com.ozz.atlas.supply.logistics.dtos.LogisticsNodeResponseDto;
import com.ozz.atlas.supply.logistics.dtos.UpdateLogisticsNodeRequestDto;
import com.ozz.atlas.supply.logistics.service.LogisticsNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/supply/logistics-nodes")
@Tag(name = "LogisticsNode", description = "창고 생성, 조회, 수정, 활성/비활성 및 창고별 재고 조회 API")
public class LogisticsNodeController {

    private final LogisticsNodeService logisticsNodeService;
    private final ItemInventoryService itemInventoryService;

    public LogisticsNodeController(LogisticsNodeService logisticsNodeService, ItemInventoryService itemInventoryService) {
        this.logisticsNodeService = logisticsNodeService;
        this.itemInventoryService = itemInventoryService;
    }

    // 물류거점 생성
    @Operation(summary = "창고 생성", description = "로그인한 조직 기준으로 창고를 생성합니다. 창고 코드는 백엔드에서 자동 생성되고, 주소 기반 좌표가 저장됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "창고 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류 또는 주소 지오코딩 실패"),
            @ApiResponse(responseCode = "403", description = "창고 생성 권한 없음")
    })
    @PostMapping
    public ResponseEntity<LogisticsNodeResponseDto> createLogisticsNode(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
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
    @Operation(summary = "창고 목록 조회", description = "로그인한 조직의 창고 목록을 최신순으로 조회합니다. 비활성 창고도 관리 목적상 함께 내려갑니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "창고 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "창고 조회 권한 없음")
    })
    @GetMapping
    public Page<LogisticsNodeResponseDto> getLogisticsNodes(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
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
    @Operation(summary = "창고 상세 조회", description = "창고 공개 식별자로 단건 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "창고 상세 조회 성공"),
            @ApiResponse(responseCode = "403", description = "창고 조회 권한 없음"),
            @ApiResponse(responseCode = "404", description = "창고를 찾을 수 없음")
    })
    @GetMapping("/{publicId}")
    public ResponseEntity<LogisticsNodeResponseDto> getLogisticsNode(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "창고 공개 식별자", example = "01HZY1NODE12345678901234")
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
    @Operation(summary = "창고 수정", description = "창고명, 주소, 상태값을 수정합니다. 주소가 변경되면 좌표도 다시 계산됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "창고 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류 또는 주소 지오코딩 실패"),
            @ApiResponse(responseCode = "403", description = "창고 수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "창고를 찾을 수 없음")
    })
    @PatchMapping("/{publicId}")
    public ResponseEntity<LogisticsNodeResponseDto> updateLogisticsNode(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "수정 요청 사용자 공개 식별자", example = "01HQUSER789ABCDEF01HQUSER")
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Parameter(description = "창고 공개 식별자", example = "01HZY1NODE12345678901234")
            @PathVariable String publicId,
            @Valid @RequestBody UpdateLogisticsNodeRequestDto dto
    ) {
        return ResponseEntity.ok(
                logisticsNodeService.updateLogisticsNode(
                        organizationPublicId,
                        organizationType,
                        userRole,
                        publicId,
                        actorUserPublicId,
                        dto
                )
        );
    }

    // 물류거점 활성화
    @Operation(summary = "창고 활성화", description = "비활성 창고를 다시 사용 가능 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "창고 활성화 성공"),
            @ApiResponse(responseCode = "403", description = "창고 활성화 권한 없음"),
            @ApiResponse(responseCode = "404", description = "창고를 찾을 수 없음")
    })
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<LogisticsNodeResponseDto> activateLogisticsNode(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "창고 공개 식별자", example = "01HZY1NODE12345678901234")
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
    @Operation(summary = "창고 비활성화", description = "창고를 비활성 처리합니다. 재고가 남아 있는 창고는 비활성화가 제한됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "창고 비활성화 성공"),
            @ApiResponse(responseCode = "400", description = "재고 존재 등 비활성화 불가 상태"),
            @ApiResponse(responseCode = "403", description = "창고 비활성화 권한 없음"),
            @ApiResponse(responseCode = "404", description = "창고를 찾을 수 없음")
    })
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<LogisticsNodeResponseDto> deactivateLogisticsNode(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "요청 사용자 역할", example = "ORG_ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Parameter(description = "창고 공개 식별자", example = "01HZY1NODE12345678901234")
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

    // 특정 물류거점(창고)에 보관 중인 재고 목록 조회
    @Operation(summary = "창고별 재고 목록 조회", description = "특정 창고에 보관 중인 재고 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "창고 재고 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "창고 재고 조회 권한 없음"),
            @ApiResponse(responseCode = "404", description = "창고를 찾을 수 없음")
    })
    @GetMapping("/{publicId}/inventories")
    public ResponseEntity<List<ItemInventoryResponse>> getNodeInventories(
            @Parameter(description = "요청 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Parameter(description = "요청 조직 유형", example = "SUPPLIER")
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Parameter(description = "창고 공개 식별자", example = "01HZY1NODE12345678901234")
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                itemInventoryService.getNodeInventories(organizationPublicId, organizationType, publicId)
        );
    }
}
