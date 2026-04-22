package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import com.ozz.atlas.auth.search.service.OrganizationSearchService;
import com.ozz.atlas.common.jpa.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.regex.Pattern;

@Service
@Transactional
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationSearchService organizationSearchService;
    private static final Pattern ORGANIZATION_ALIAS_PATTERN = Pattern.compile("^[A-Z0-9]{2,10}$");

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository,
                               OrganizationSearchService organizationSearchService) {
        this.organizationRepository = organizationRepository;
        this.organizationSearchService = organizationSearchService;
    }

    // 조직 생성
    // organizationAlias는 물류거점/후속 코드 생성의 기준값이므로 저장 전에 대문자 정규화와 중복 검증을 수행한다.
    public String createOrganization(OrganizationCreateDto dto) {
        String normalizedAlias = normalizeOrganizationAlias(dto.getOrganizationAlias());
        validateOrganizationAlias(normalizedAlias);

        if (organizationRepository.existsByOrganizationAlias(normalizedAlias)) {
            throw new IllegalArgumentException("이미 사용 중인 조직 코드입니다.");
        }

        dto.setOrganizationAlias(normalizedAlias);

        Organization organization = organizationRepository.save(dto.toEntity());
        organizationSearchService.saveOrganizationDocument(organization);
        return organization.getPublicId();
    }


    // 조직 목록 조회
    public Page<OrganizationListDto> organizationList(Pageable pageable, OrganizationSearchDto searchDto) {
        // 검색 조건이 하나라도 있으면 Elasticsearch 통합검색을 사용
        // 조건이 전혀 없을 때만 기본 DB 목록 조회로 내려감
        if (hasSearchCondition(searchDto)) {
            return organizationSearchService.search(pageable, searchDto);
        }

        return organizationRepository.findAll(pageable)
                .map(OrganizationListDto::fromEntity);
    }

    // 조직 상세 조회
    public OrganizationDetailDto organizationDetail(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조직입니다."));

        if (organization.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 조직입니다.");
        }

        return OrganizationDetailDto.fromEntity(organization);
    }

    // 조직 정보 수정
    public OrganizationDetailDto organizationUpdate(Long organizationId,
                                                    OrganizationUpdateDto dto,
                                                    AuthPrincipal principal) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조직입니다."));

        if (organization.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 조직입니다.");
        }

        boolean isAdmin = principal.role() == UserRole.ADMIN;
        boolean isOrgAdmin = principal.role() == UserRole.ORG_ADMIN
                && principal.organizationPublicId().equals(organization.getPublicId());

        if (!isAdmin && !isOrgAdmin) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        // 수정 시에도 동일한 alias 규칙을 적용하고, 현재 조직을 제외한 중복만 차단한다.
        if (hasText(dto.getOrganizationAlias())) {
            String normalizedAlias = normalizeOrganizationAlias(dto.getOrganizationAlias());
            validateOrganizationAlias(normalizedAlias);

            if (organizationRepository.existsByOrganizationAliasAndOrganizationIdNot(normalizedAlias, organizationId)) {
                throw new IllegalArgumentException("이미 사용 중인 조직 코드입니다.");
            }

            dto.setOrganizationAlias(normalizedAlias);
        }

        organization.updateOrganization(dto);
        organizationSearchService.saveOrganizationDocument(organization);

        return OrganizationDetailDto.fromEntity(organization);
    }

    // 조직 삭제
    public void organizationDelete(Long organizationId, AuthPrincipal principal) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조직입니다."));

        boolean isAdmin = principal.role() == UserRole.ADMIN;
        boolean isOrgAdmin = principal.role() == UserRole.ORG_ADMIN
                && principal.organizationPublicId().equals(organization.getPublicId());

        if (!isAdmin && !isOrgAdmin) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        if (organization.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 조직입니다.");
        }

        organization.deleteOrganization();
        organizationSearchService.saveOrganizationDocument(organization);
    }

    // 검색 DTO에 실제 검색 조건이 하나라도 들어왔는지 확인
    // keyword 뿐 아니라 조직유형, 조직명, 상태도 모두 통합검색 진입 조건으로 봄
    private boolean hasSearchCondition(OrganizationSearchDto searchDto) {
        if (searchDto == null) {
            return false;
        }

        return searchDto.getOrganizationType() != null
                || hasText(searchDto.getOrganizationName())
                || hasText(searchDto.getOrganizationEnglishName())
                || hasText(searchDto.getOrganizationAlias())
                || searchDto.getStatus() != null
                || hasText(searchDto.getKeyword());
    }

//    사용자가 소문자나 공백을 포함해 입력해도 저장 기준은 대문자 코드로 통일한다.
    private String normalizeOrganizationAlias(String organizationAlias) {
        if (organizationAlias == null) {
            return null;
        }
        return organizationAlias.trim().toUpperCase();
    }

//    alias는 사람이 읽을 수 있는 짧은 조직 코드로 사용되므로 대문자/숫자 2~10자리만 허용한다.
    private void validateOrganizationAlias(String organizationAlias) {
        if (!hasText(organizationAlias)) {
            throw new IllegalArgumentException("조직 코드는 비어있으면 안 됩니다.");
        }

        if (!ORGANIZATION_ALIAS_PATTERN.matcher(organizationAlias).matches()) {
            throw new IllegalArgumentException("조직 코드는 대문자와 숫자만 사용하여 2자 이상 10자 이하로 입력해야 합니다.");
        }
    }

    // 문자열 값이 null 이거나 공백인지 확인하는 공통 메서드
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
