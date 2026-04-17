package com.ozz.atlas.file.dtos;

import com.ozz.atlas.file.domain.RefType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "첨부 묶음 생성 요청")
public class CreateAttachmentRequestDto {
    @Schema(description = "연결 대상 타입", example = "ITEM")
    private RefType refType;

    @Schema(description = "연결 대상 공개 식별자", example = "item_01HZY2ITEM123456789")
    private String refPublicId;
}
