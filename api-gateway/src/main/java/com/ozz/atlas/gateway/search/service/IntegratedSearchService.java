package com.ozz.atlas.gateway.search.service;

import com.ozz.atlas.gateway.search.dtos.IntegratedSearchItemDto;
import com.ozz.atlas.gateway.search.dtos.IntegratedSearchRequestDto;
import com.ozz.atlas.gateway.search.dtos.IntegratedSearchResponseDto;
import com.ozz.atlas.gateway.search.dtos.IntegratedSearchSectionDto;
import com.ozz.atlas.gateway.search.dtos.IntegratedSearchSectionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IntegratedSearchService {

    // auth-service, supply-service 검색 API 호출에 사용
    private final WebClient webClient;

    // auth-service 실제 주소
    @Value("${auth-service.url}")
    private String authServiceUrl;

    // supply-service 실제 주소
    @Value("${supply-service.url}")
    private String supplyServiceUrl;

    // 게이트웨이 최종 통합검색 메인 진입점
    public Mono<IntegratedSearchResponseDto> search(
            IntegratedSearchRequestDto request,
            String authorization,
            String organizationPublicId,
            String organizationType
    ) {
        String keyword = request != null ? request.getKeyword() : null;

        // 검색어가 비어 있으면 downstream 호출 없이 빈 결과를 바로 반환
        if (!hasText(keyword)) {
            return Mono.just(
                    IntegratedSearchResponseDto.builder()
                            .keyword(keyword)
                            .sections(List.of())
                            .build()
            );
        }

        int size = normalizeSize(request != null ? request.getSize() : null);
        int fetchSize = buildFetchSize(size);

        // 사용자, 조직, 업무 검색을 병렬로 호출한 뒤 하나로 합침
        return Mono.zip(
                        searchUsers(keyword, size, fetchSize, authorization),
                        searchOrganizations(keyword, size, fetchSize, authorization),
                        searchSupply(keyword, size, authorization, organizationPublicId, organizationType)
                )
                .map(tuple -> {
                    List<IntegratedSearchSectionDto> sections = new ArrayList<>();
                    sections.addAll(tuple.getT1());
                    sections.addAll(tuple.getT2());
                    sections.addAll(tuple.getT3());

                    return IntegratedSearchResponseDto.builder()
                            .keyword(keyword)
                            .sections(sections)
                            .build();
                });
    }

    // auth-service 사용자 검색 결과를 사용자 섹션으로 바꿈
    private Mono<List<IntegratedSearchSectionDto>> searchUsers(
            String keyword,
            int size,
            int fetchSize,
            String authorization
    ) {
        return webClient.get()
                .uri(authServiceUrl + "/api/auth/users?keyword={keyword}&page=0&size={size}", keyword, fetchSize)
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<AuthUserItem>>() {})
                .map(page -> buildUserSection(page, keyword, size))
                .onErrorReturn(List.of());
    }

    // auth-service 조직 검색 결과를 조직 섹션으로 바꿈
    private Mono<List<IntegratedSearchSectionDto>> searchOrganizations(
            String keyword,
            int size,
            int fetchSize,
            String authorization
    ) {
        return webClient.get()
                .uri(authServiceUrl + "/api/auth/organizations?keyword={keyword}&page=0&size={size}", keyword, fetchSize)
                .headers(headers -> applyAuthorization(headers, authorization))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageResponse<AuthOrganizationItem>>() {})
                .map(page -> buildOrganizationSection(page, keyword, size))
                .onErrorReturn(List.of());
    }

    // supply-service 업무 통합검색 결과를 게이트웨이 섹션으로 옮김
    private Mono<List<IntegratedSearchSectionDto>> searchSupply(
            String keyword,
            int size,
            String authorization,
            String organizationPublicId,
            String organizationType
    ) {
        return webClient.get()
                .uri(supplyServiceUrl + "/api/supply/search?keyword={keyword}&size={size}", keyword, size)
                .headers(headers -> {
                    applyAuthorization(headers, authorization);
                    applyHeaderIfPresent(headers, "X-Organization-Public-Id", organizationPublicId);
                    applyHeaderIfPresent(headers, "X-Organization-Type", organizationType);
                })
                .retrieve()
                .bodyToMono(SupplyIntegratedSearchResponse.class)
                .map(this::buildSupplySections)
                .onErrorReturn(List.of());
    }

    // 사용자 응답을 사용자 섹션 한 개로 변환
    private List<IntegratedSearchSectionDto> buildUserSection(
            PageResponse<AuthUserItem> page,
            String keyword,
            int size
    ) {
        if (page == null || page.getContent() == null || page.getContent().isEmpty()) {
            return List.of();
        }

        // 이름, 로그인 ID, 이메일, 전화번호, 직책 기준으로 한 번 더 엄격하게 필터링
        List<IntegratedSearchItemDto> items = page.getContent().stream()
                .filter(user -> matchesKeyword(
                        keyword,
                        buildUserName(user),
                        user.getLoginId(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getJobTitle()
                ))
                .map(user -> IntegratedSearchItemDto.builder()
                        .type(IntegratedSearchSectionType.USER)
                        .publicId(user.getUserPublicId())
                        .title(buildUserName(user))
                        .subtitle(buildUserSubtitle(user))
                        .status(null)
                        .build())
                .limit(size)
                .toList();

        if (items.isEmpty()) {
            return List.of();
        }

        return List.of(buildSection(IntegratedSearchSectionType.USER, items.size(), items));
    }

    // 조직 응답을 조직 섹션 한 개로 변환
    private List<IntegratedSearchSectionDto> buildOrganizationSection(
            PageResponse<AuthOrganizationItem> page,
            String keyword,
            int size
    ) {
        if (page == null || page.getContent() == null || page.getContent().isEmpty()) {
            return List.of();
        }

        // 조직명, 담당자명, 이메일, 전화번호 기준으로 한 번 더 엄격하게 필터링
        List<IntegratedSearchItemDto> items = page.getContent().stream()
                .filter(organization -> matchesKeyword(
                        keyword,
                        organization.getOrganizationName(),
                        buildOrganizationContactName(organization),
                        organization.getContactEmail(),
                        organization.getContactPhone()
                ))
                .map(organization -> IntegratedSearchItemDto.builder()
                        .type(IntegratedSearchSectionType.ORGANIZATION)
                        .publicId(organization.getOrganizationPublicId())
                        .title(organization.getOrganizationName())
                        .subtitle(buildOrganizationSubtitle(organization))
                        .status(organization.getStatus())
                        .build())
                .limit(size)
                .toList();

        if (items.isEmpty()) {
            return List.of();
        }

        return List.of(buildSection(IntegratedSearchSectionType.ORGANIZATION, items.size(), items));
    }

    // supply-service 섹션들을 게이트웨이 섹션으로 그대로 옮김
    private List<IntegratedSearchSectionDto> buildSupplySections(SupplyIntegratedSearchResponse response) {
        if (response == null || response.getSections() == null || response.getSections().isEmpty()) {
            return List.of();
        }

        return response.getSections().stream()
                .map(section -> {
                    IntegratedSearchSectionType type = resolveSectionType(section.getType());

                    if (type == null || section.getItems() == null || section.getItems().isEmpty()) {
                        return null;
                    }

                    List<IntegratedSearchItemDto> items = section.getItems().stream()
                            .map(item -> IntegratedSearchItemDto.builder()
                                    .type(type)
                                    .id(item.getId())
                                    .publicId(item.getPublicId())
                                    .title(item.getTitle())
                                    .subtitle(item.getSubtitle())
                                    .status(item.getStatus())
                                    .build())
                            .toList();

                    return IntegratedSearchSectionDto.builder()
                            .type(type)
                            .label(hasText(section.getLabel()) ? section.getLabel() : type.getLabel())
                            .totalCount(section.getTotalCount())
                            .items(items)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    // 사용자 카드 제목은 사람이 읽기 쉬운 이름으로 만듬
    private String buildUserName(AuthUserItem user) {
        String fullName = joinTexts(user.getLastName(), user.getMiddleName(), user.getFirstName());
        return hasText(fullName) ? fullName : user.getLoginId();
    }

    // 사용자 카드 보조 설명은 로그인 ID와 역할 중심으로 만듬
    private String buildUserSubtitle(AuthUserItem user) {
        return joinTexts(user.getLoginId(), user.getJobTitle(), user.getUserRole());
    }

    // 조직 담당자 이름을 보기 쉬운 문자열로 만듬
    private String buildOrganizationContactName(AuthOrganizationItem organization) {
        return joinTexts(
                organization.getContactLastName(),
                organization.getContactMiddleName(),
                organization.getContactFirstName()
        );
    }

    // 조직 카드 보조 설명은 조직 유형과 tier 정보를 묶어서 만듬
    private String buildOrganizationSubtitle(AuthOrganizationItem organization) {
        List<String> parts = new ArrayList<>();

        if (hasText(organization.getOrganizationType())) {
            parts.add(organization.getOrganizationType());
        }

        String contactName = buildOrganizationContactName(organization);
        if (hasText(contactName)) {
            parts.add(contactName);
        }

        return String.join(" / ", parts);
    }

    // 섹션 공통 구조를 만드는 헬퍼
    private IntegratedSearchSectionDto buildSection(
            IntegratedSearchSectionType type,
            long totalCount,
            List<IntegratedSearchItemDto> items
    ) {
        return IntegratedSearchSectionDto.builder()
                .type(type)
                .label(type.getLabel())
                .totalCount(totalCount)
                .items(items)
                .build();
    }

    // 섹션당 개수는 너무 크지 않게 제한
    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 5;
        }

        return Math.min(size, 10);
    }

    // auth 검색은 2차 필터링을 하기 때문에 처음에는 조금 더 많이 가져옴
    private int buildFetchSize(int size) {
        return Math.max(size * 5, 20);
    }

    // title, subtitle 같은 대표 필드에 keyword가 실제로 들어있는지 다시 확인
    private boolean matchesKeyword(String keyword, String... values) {
        String normalizedKeyword = normalize(keyword);

        if (normalizedKeyword.isBlank()) {
            return false;
        }

        return Arrays.stream(values)
                .filter(this::hasText)
                .map(this::normalize)
                .anyMatch(value -> value.contains(normalizedKeyword));
    }

    // 대소문자, 공백, 하이픈, 언더바 차이를 줄이기 위해 정규화
    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase()
                .replaceAll("[\\s\\-_]", "");
    }

    // 문자열이 비어 있지 않은지 확인
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // 비어 있지 않은 문자열만 모아서 보조 설명으로 만듬
    private String joinTexts(String... values) {
        return Arrays.stream(values)
                .filter(this::hasText)
                .collect(Collectors.joining(" / "));
    }

    // downstream 호출 시 Authorization 헤더를 그대로 넘김
    private void applyAuthorization(HttpHeaders headers, String authorization) {
        applyHeaderIfPresent(headers, HttpHeaders.AUTHORIZATION, authorization);
    }

    // 값이 있을 때만 헤더를 붙임
    private void applyHeaderIfPresent(HttpHeaders headers, String headerName, String value) {
        if (hasText(value)) {
            headers.set(headerName, value);
        }
    }

    // supply-service 섹션 타입 문자열을 게이트웨이 enum으로 변환
    private IntegratedSearchSectionType resolveSectionType(String type) {
        if (!hasText(type)) {
            return null;
        }

        try {
            return IntegratedSearchSectionType.valueOf(type);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    // auth-service Page 응답에서 content만 읽기 위한 내부 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PageResponse<T> {
        private List<T> content;
    }

    // auth-service 사용자 검색 응답 항목을 받기 위한 내부 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AuthUserItem {
        private String userPublicId;
        private String organizationPublicId;
        private String loginId;
        private String firstName;
        private String middleName;
        private String lastName;
        private String email;
        private String phone;
        private String jobTitle;
        private String userRole;
    }

    // auth-service 조직 검색 응답 항목을 받기 위한 내부 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AuthOrganizationItem {
        private String organizationPublicId;
        private String organizationType;
        private String organizationName;
        private String contactFirstName;
        private String contactMiddleName;
        private String contactLastName;
        private String contactEmail;
        private String contactPhone;
        private String status;
    }

    // supply-service 통합검색 전체 응답을 받기 위한 내부 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SupplyIntegratedSearchResponse {
        private String keyword;
        private List<SupplyIntegratedSearchSection> sections;
    }

    // supply-service 통합검색 섹션을 받기 위한 내부 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SupplyIntegratedSearchSection {
        private String type;
        private String label;
        private long totalCount;
        private List<SupplyIntegratedSearchItem> items;
    }

    // supply-service 통합검색 아이템을 받기 위한 내부 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SupplyIntegratedSearchItem {
        private String type;
        private Long id;
        private String publicId;
        private String title;
        private String subtitle;
        private String status;
    }
}
