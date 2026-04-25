package com.ozz.atlas.auth.dtos.user;

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
public class OrganizationUserExcelUploadResponseDto {

    // 총 행 개수
    private int totalCount;

    // 성공 개수
    private int successCount;

    // 실패 개수
    private int failCount;

    // 줄별 처리 결과
    private List<RowResult> results;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RowResult {
        // 엑셀 몇 번째 줄인지 표시
        private int rowNumber;

        // 성공 여부
        private boolean success;

        // 성공 시 생성된 사용자 공개 ID
        private String userPublicId;

        // 성공 시 자동 생성된 로그인 ID
        private String loginId;

        // 성공 시 자동 생성된 임시 비밀번호
        private String temporaryPassword;

        // 실패 시 에러 문구
        private String message;
    }
}
