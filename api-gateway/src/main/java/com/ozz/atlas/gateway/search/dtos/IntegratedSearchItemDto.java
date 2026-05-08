package com.ozz.atlas.gateway.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Integrated Search Item 값 모델")
public class IntegratedSearchItemDto {

    // 이 결과가 어느 섹션에 속하는지 알려줌
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private IntegratedSearchSectionType type;

    // 숫자 ID를 쓰는 도메인용 값
    @Schema(description = "식별자", example = "1", nullable = true)
    private Long id;

    // publicId를 쓰는 도메인용 값
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;

    // 카드에서 가장 크게 보여줄 대표 제목
    @Schema(description = "제목", example = "샘플 이름", nullable = true)
    private String title;

    // 제목 아래에 보여줄 보조 설명
    @Schema(description = "제목", example = "샘플 이름", nullable = true)
    private String subtitle;

    // 검색 결과 카드 왼쪽에 보여줄 공통 썸네일 이미지 경로
// 사용자 프로필, 품목 이미지, 조직 로고 등 다양한 타입에서 공통
    @Schema(description = "thumbnail Url 값", example = "sample", nullable = true)
    private String thumbnailUrl;

    // 상태값이 있으면 문자열로 내려줌
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private String status;

    // 조직 영문명을 프론트 조직 프로필 화면으로 넘길 때 사용
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String organizationEnglishName;

    // 조직 담당자 이메일을 프론트 조직 프로필 화면으로 넘길 때 사용
    @Schema(description = "이메일", example = "user@atlas.com", nullable = true)
    private String contactEmail;

    // 조직 담당자 연락처를 프론트 조직 프로필 화면으로 넘길 때 사용
    @Schema(description = "연락처", example = "010-1234-5678", nullable = true)
    private String contactPhone;

    // 조직 담당자명을 프론트 조직 프로필 화면으로 넘길 때 사용
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String contactName;

    // 조직 주소를 통합검색 결과와 조직 프로필 화면으로 넘길 때 사용
    @Schema(description = "address 값", example = "sample", nullable = true)
    private String address;

    // 조직 상세주소를 통합검색 결과와 조직 프로필 화면으로 넘길 때 사용
    @Schema(description = "address Detail 값", example = "sample", nullable = true)
    private String addressDetail;

    // 조직 우편번호를 통합검색 결과와 조직 프로필 화면으로 넘길 때 사용
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String zipCode;

}
