package com.ozz.atlas.auth.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 사원 엑셀 업로드 전체 결과
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Organization User Excel Upload 값 응답")
public class OrganizationUserExcelUploadResponseDto {

    // 총 행 개수
    @Schema(description = "개수", example = "1", nullable = true)
    private int totalCount;

    // 성공 개수
    @Schema(description = "개수", example = "1", nullable = true)
    private int successCount;

    // 실패 개수
    @Schema(description = "개수", example = "1", nullable = true)
    private int failCount;

    // 줄별 처리 결과
    @Schema(description = "results 값", nullable = true)
    private List<RowResult> results;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RowResult {
        // 엑셀 몇 번째 줄인지 표시
        @Schema(description = "번호", example = "NO-2026-0001", nullable = true)
        private int rowNumber;

        // 성공 여부
        @Schema(description = "success 값", example = "true", nullable = true)
        private boolean success;

        // 성공 시 생성된 사용자 공개 ID
        @Schema(description = "사용자 공개 식별자", example = "sample_public_id", nullable = true)
        private String userPublicId;

        // 성공 시 자동 생성된 로그인 ID
        @Schema(description = "식별자", example = "1", nullable = true)
        private String loginId;

        // 성공 시 자동 생성된 임시 비밀번호
        @Schema(description = "temporary Password 값", example = "sample", nullable = true)
        private String temporaryPassword;

        // 실패 시 에러 문구
        @Schema(description = "메시지", example = "샘플 내용", nullable = true)
        private String message;
    }
}
