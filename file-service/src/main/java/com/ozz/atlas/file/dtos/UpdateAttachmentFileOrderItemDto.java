package com.ozz.atlas.file.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "날짜 모델")
public class UpdateAttachmentFileOrderItemDto {
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String filePublicId;
    @Schema(description = "sort Order 값", example = "1", nullable = true)
    private Integer sortOrder;
}
