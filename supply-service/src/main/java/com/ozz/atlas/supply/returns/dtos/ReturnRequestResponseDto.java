package com.ozz.atlas.supply.returns.dtos;

import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.domain.ReturnType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "반품 요청 상세 응답")
public class ReturnRequestResponseDto {
    @Schema(description = "반품 내부 ID", example = "7")
    private Long id;
    @Schema(description = "반품 공개 식별자", example = "ret_01HZY2RET123456789")
    private String publicId;
    @Schema(description = "반품 번호", example = "RET-2026-0007")
    private String returnNumber;
    @Schema(description = "원본 출하 공개 식별자", example = "ship_01HZY1SHIPMENT123456789", nullable = true)
    private String sourceShipmentPublicId;
    @Schema(description = "반품 요청 조직 공개 식별자", example = "org_req_01HZY2ORGREQ123456")
    private String requestOrganizationPublicId;
    @Schema(description = "반품 대상 조직 공개 식별자", example = "org_tgt_01HZY2ORGTGT123456")
    private String targetOrganizationPublicId;
    @Schema(description = "반품 유형", example = "QUALITY_ISSUE")
    private ReturnType returnType;
    @Schema(description = "반품 사유", example = "유통기한 임박 품목 회수")
    private String returnReason;
    @Schema(description = "반품 상태", example = "REQUESTED")
    private ReturnStatus returnStatus;
    @Schema(description = "반품 요청 시각", example = "2026-04-17T10:10:00")
    private LocalDateTime requestedAt;
    @Schema(description = "반품 승인 시각", example = "2026-04-17T11:00:00", nullable = true)
    private LocalDateTime approvedAt;
    @Schema(description = "반품 완료 시각", example = "2026-04-18T16:30:00", nullable = true)
    private LocalDateTime completedAt;
    @Schema(description = "생성자 사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String createdByUserPublicId;
    @Schema(description = "첨부 파일 공개 식별자 목록", example = "[\"att_01HZY2ATT10\"]")
    private List<String> attachmentPublicIds;
    @Schema(description = "반품 품목 목록")
    private List<ReturnItemResponseDto> items;

    public static ReturnRequestResponseDto from(ReturnRequest entity) {
        List<String> attachments = (entity.getAttachmentPublicIds() != null && !entity.getAttachmentPublicIds().isBlank())
                ? Arrays.asList(entity.getAttachmentPublicIds().split(","))
                : Collections.emptyList();

        return ReturnRequestResponseDto.builder()
                .id(entity.getId())
                .publicId(entity.getPublicId())
                .returnNumber(entity.getReturnNumber())
                .sourceShipmentPublicId(entity.getSourceShipmentPublicId())
                .requestOrganizationPublicId(entity.getRequestOrganizationPublicId())
                .targetOrganizationPublicId(entity.getTargetOrganizationPublicId())
                .returnType(entity.getReturnType())
                .returnReason(entity.getReturnReason())
                .returnStatus(entity.getReturnStatus())
                .requestedAt(entity.getRequestedAt())
                .approvedAt(entity.getApprovedAt())
                .completedAt(entity.getCompletedAt())
                .createdByUserPublicId(entity.getCreatedByUserPublicId())
                .attachmentPublicIds(attachments)
                .items(entity.getItems().stream().map(ReturnItemResponseDto::from).collect(Collectors.toList()))
                .build();
    }
}
