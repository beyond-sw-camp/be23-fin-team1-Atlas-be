package com.ozz.atlas.file.dtos;

import com.ozz.atlas.file.domain.RefType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "첨부 묶음 응답")
public class AttachmentResponseDto {
    @Schema(description = "첨부 묶음 공개 식별자", example = "att_01HZY3ATT123456789")
    private String attachmentPublicId;
    @Schema(description = "연결 대상 타입", example = "ITEM")
    private RefType refType;
    @Schema(description = "연결 대상 공개 식별자", example = "item_01HZY2ITEM123456789")
    private String refPublicId;
    @Schema(description = "업로드 사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String uploadedByUserPublicId;

    @Builder.Default
    @Schema(description = "첨부된 파일 목록")
    private List<FileResponseDto> files = new ArrayList<>();
}
