package com.ozz.atlas.supply.returns.dtos;

import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.domain.ReturnType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReturnRequestResponseDto {
    private Long id;
    private String publicId;
    private String returnNumber;
    private String sourceShipmentPublicId;
    private String requestOrganizationPublicId;
    private String targetOrganizationPublicId;
    private ReturnType returnType;
    private String returnReason;
    private ReturnStatus returnStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime completedAt;
    private String createdByUserPublicId;
    private List<String> attachmentPublicIds;
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