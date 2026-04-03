package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    //    조직 생성
    public String createOrganization(OrganizationCreateDto dto) {
        Organization organization = organizationRepository.save(dto.toEntity());
        return organization.getPublicId();
    }

    //    조직 목록 조회
    public Page<OrganizationListDto> organizationList(Pageable pageable, OrganizationSearchDto searchDto) {
        Specification<Organization> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchDto.getOrganizationType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("organizationType"), searchDto.getOrganizationType()));
            }

            if (searchDto.getOrganizationName() != null && !searchDto.getOrganizationName().isBlank()) {
                predicates.add(criteriaBuilder.like(root.get("organizationName"), "%" + searchDto.getOrganizationName() + "%"));
            }

            if (searchDto.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchDto.getStatus()));
            } else {
                predicates.add(criteriaBuilder.equal(root.get("status"), Status.ACTIVE));
            }


            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return organizationRepository.findAll(specification, pageable)
                .map(OrganizationListDto::fromEntity);
    }

    //    조직 상세 조회
    public OrganizationDetailDto organizationDetail(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조직입니다."));
        if (organization.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 조직입니다.");
        }


        return OrganizationDetailDto.fromEntity(organization);
    }

    //    조직 정보 수정
    public OrganizationDetailDto organizationUpdate(Long organizationId, OrganizationUpdateDto dto, AuthPrincipal principal) {
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
        return OrganizationDetailDto.fromEntity(organization);
    }

    //    조직 삭제
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
    }

}