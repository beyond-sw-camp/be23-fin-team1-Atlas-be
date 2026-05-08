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
@Schema(description = "날짜 요청")
public class UpdateAttachmentFileRequestDto {

    // KEEP, DELETE일 때 사용
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String filePublicId;

    // ADD일 때 사용
    // multipart files 파트의 0-based 순번과 매핑
    @Schema(description = "upload Index 값", example = "1", nullable = true)
    private Integer uploadIndex;

    // KEEP, ADD일 때 사용
    // attachment 내 최종 파일 순서
    @Schema(description = "sort Order 값", example = "1", nullable = true)
    private Integer sortOrder;

    // KEEP: 기존 파일 유지 및 정렬 변경
    // DELETE: 기존 파일 삭제
    // ADD: multipart files 파트의 새 파일 추가
    @Schema(description = "action 값", example = "sample", nullable = true)
    private AttachmentFileUpdateAction action;
}
