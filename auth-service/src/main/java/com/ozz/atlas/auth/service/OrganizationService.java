package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.OrganizationType;
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

@Service
@Transactional
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationSearchService organizationSearchService;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository,
                               OrganizationSearchService organizationSearchService) {
        this.organizationRepository = organizationRepository;
        this.organizationSearchService = organizationSearchService;
    }

    // 조직 생성
    public String createOrganization(OrganizationCreateDto dto) {
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
                || searchDto.getStatus() != null
                || hasText(searchDto.getKeyword());
    }

    // 문자열 값이 null 이거나 공백인지 확인하는 공통 메서드
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
