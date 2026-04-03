package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.*;
import com.ozz.atlas.auth.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/auth")
public class OrganizationController {
    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    //    조직 등록
    @PostMapping("/organizations")
    public ResponseEntity<OrganizationCreateResponseDto> createOrganization(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid OrganizationCreateDto dto) {

        if (principal.role() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String organizationPublicId = organizationService.createOrganization(dto);

        OrganizationCreateResponseDto response = OrganizationCreateResponseDto.builder()
                .organizationPublicId(organizationPublicId)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //    조직 목록 조회
    @GetMapping("/organizations")
    public ResponseEntity<Page<OrganizationListDto>> organizationList(
            @PageableDefault(size = 10, sort = "organizationId", direction = Sort.Direction.DESC) Pageable pageable,
            @ModelAttribute OrganizationSearchDto organizationSearchDto) {

        Page<OrganizationListDto> response = organizationService.organizationList(pageable, organizationSearchDto);
        return ResponseEntity.ok(response);
    }

    //    조직 살세 조회
    @GetMapping("/organizations/{organizationId}")
    public ResponseEntity<OrganizationDetailDto> organizationDetail(@PathVariable Long organizationId) {
        OrganizationDetailDto response = organizationService.organizationDetail(organizationId);
        return ResponseEntity.ok(response);
    }

    //    조직 정보 수정
    @PatchMapping("/organizations/{organizationId}")
    public ResponseEntity<OrganizationDetailDto> organizationUpdate(
            @PathVariable Long organizationId,
            @RequestBody @Valid OrganizationUpdateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal) {

        OrganizationDetailDto organization = organizationService.organizationDetail(organizationId);

        boolean isAdmin = principal.role() == UserRole.ADMIN;
        boolean isOrgAdmin = principal.role() == UserRole.ORG_ADMIN
                && principal.organizationPublicId().equals(organization.getOrganizationPublicId());

        if (!isAdmin && !isOrgAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        OrganizationDetailDto response = organizationService.organizationUpdate(organizationId, dto, principal);
        return ResponseEntity.ok(response);
    }

    //    조직 삭제
    @DeleteMapping("/organizations/{organizationId}")
    public ResponseEntity<Void> organizationDelete(
            @PathVariable Long organizationId,
            @AuthenticationPrincipal AuthPrincipal principal) {

        OrganizationDetailDto organization = organizationService.organizationDetail(organizationId);

        boolean isAdmin = principal.role() == UserRole.ADMIN;
        boolean isOrgAdmin = principal.role() == UserRole.ORG_ADMIN
                && principal.organizationPublicId().equals(organization.getOrganizationPublicId());

        if (!isAdmin && !isOrgAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        organizationService.organizationDelete(organizationId, principal);
        return ResponseEntity.noContent().build();
    }

}
