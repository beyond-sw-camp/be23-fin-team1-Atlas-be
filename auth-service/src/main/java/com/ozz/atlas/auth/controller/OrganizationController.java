package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.organization.*;
import com.ozz.atlas.auth.dtos.user.OrganizationUserCreateDto;
import com.ozz.atlas.auth.dtos.user.ProvisionedUserResponseDto;
import com.ozz.atlas.auth.search.dtos.OrganizationSearchDto;
import com.ozz.atlas.auth.service.OrganizationService;
import com.ozz.atlas.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Organizations")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {
    private final OrganizationService organizationService;
    private final UserService userService;

    @Autowired
    public OrganizationController(OrganizationService organizationService, UserService userService) {
        this.organizationService = organizationService;
        this.userService = userService;
    }

    //    조직 등록
    @PostMapping("/organizations")
    @Operation(
            summary = "조직 등록",
            description = "발주사 또는 협력사 조직을 신규 등록한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrganizationCreateDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "organizationType": "SUPPLIER",
                                              "organizationName": "아틀라스 푸드 서플라이어",
                                              "organizationEnglishName": "Atlas Foods Supplier",
                                              "organizationAlias": "AFS",
                                              "businessNo": "123-45-67890",
                                              "contactFirstName": "Minji",
                                              "contactMiddleName": "J",
                                              "contactLastName": "Kim",
                                              "contactEmail": "minji.kim@atlasfoods.com",
                                              "contactPhone": "010-1234-5678",
                                            }
                                            """
                            )
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "201",
                    description = "조직 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = OrganizationCreateResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "organizationPublicId": "org_01HZX9X5D4P2Q7F8R9S1T2U3V4"
                                            }
                                            """
                            )
                    )
            )
    )
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
    // 관리자가 조직의 최초 ORG_ADMIN 계정을 생성
    @PostMapping("/organizations/{organizationPublicId}/org-admin")
    @Operation(
            summary = "조직 대표자 계정 생성",
            description = "플랫폼 관리자가 조직의 최초 ORG_ADMIN 계정을 생성한다."
    )
    public ResponseEntity<ProvisionedUserResponseDto> createInitialOrgAdmin(
            @PathVariable String organizationPublicId,
            @RequestBody @Valid OrganizationUserCreateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        if (principal.role() != UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProvisionedUserResponseDto response =
                userService.createInitialOrgAdmin(organizationPublicId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //    조직 목록 조회
    @GetMapping("/organizations")
    @Operation(
            summary = "조직 목록 조회",
            description = "조직 목록을 페이지 단위로 조회하고 검색 조건이 있으면 통합검색을 적용한다."
    )
    public ResponseEntity<Page<OrganizationListDto>> organizationList(
            @PageableDefault(size = 10, sort = "organizationId", direction = Sort.Direction.DESC) Pageable pageable,
            @ModelAttribute OrganizationSearchDto organizationSearchDto) {

        Page<OrganizationListDto> response = organizationService.organizationList(pageable, organizationSearchDto);
        return ResponseEntity.ok(response);
    }

    //    조직 상세 조회
    @GetMapping("/organizations/{organizationId}")
    @Operation(
            summary = "조직 상세 조회",
            description = "조직 내부 ID 기준으로 상세 정보를 조회한다."
    )
    public ResponseEntity<OrganizationDetailDto> organizationDetail(@PathVariable Long organizationId) {
        OrganizationDetailDto response = organizationService.organizationDetail(organizationId);
        return ResponseEntity.ok(response);
    }

    // supply-service에서 물류거점 코드 생성을 위해 organization alias만 조회할 때 사용한다.
    @GetMapping("/organizations/public/{organizationPublicId}/alias")
    @Operation(
            summary = "조직 별칭 조회",
            description = "내부 연동용으로 조직 공개 ID 기준 조직 별칭을 조회한다."
    )
    public ResponseEntity<OrganizationAliasLookupDto> organizationAliasByPublicId(
            @PathVariable String organizationPublicId
    ) {
        OrganizationAliasLookupDto response = organizationService.organizationAliasByPublicId(organizationPublicId);
        return ResponseEntity.ok(response);
    }

    //    조직 정보 수정
    @PatchMapping("/organizations/{organizationId}")
    @Operation(
            summary = "조직 정보 수정",
            description = "플랫폼 관리자 또는 해당 조직 대표자가 조직 정보를 수정한다."
    )
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

    // 조직 상태를 활성화, 비활성화, 삭제 중 하나로 변경
    @PatchMapping("/organizations/{organizationId}/status")
    @Operation(
            summary = "조직 상태 변경",
            description = "조직 상태를 활성화, 비활성화, 삭제 중 하나로 변경한다."
    )
    public ResponseEntity<OrganizationDetailDto> organizationStatusUpdate(
            @PathVariable Long organizationId,
            @RequestBody @Valid OrganizationStatusUpdateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal) {

        OrganizationDetailDto response =
                organizationService.organizationStatusUpdate(organizationId, dto, principal);
        return ResponseEntity.ok(response);
    }

    // 현재 로그인한 사용자의 조직 상세를 조회
    @GetMapping("/organizations/me")
    @Operation(
            summary = "내 조직 상세 조회",
            description = "현재 로그인한 사용자의 소속 조직 상세 정보를 조회한다."
    )
    public ResponseEntity<OrganizationDetailDto> myOrganizationDetail(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        // 로그인한 사용자의 organizationPublicId 로 자기 조직을 조회합니다.
        OrganizationDetailDto response =
                organizationService.organizationDetailByPublicId(principal.organizationPublicId());

        return ResponseEntity.ok(response);
    }

    // supply-service에서 정산 엑셀에 조직명을 표시하기 위해 organizationPublicId로 조직명을 조회
    @GetMapping("/organizations/public/{organizationPublicId}/name")
    @Operation(
            summary = "조직명 조회",
            description = "내부 서비스 연동용으로 조직 공개 ID 기준 조직명을 조회합니다."
    )
    public ResponseEntity<OrganizationNameLookupDto> organizationNameByPublicId(
            @PathVariable String organizationPublicId
    ) {
        OrganizationNameLookupDto response =
                organizationService.organizationNameByPublicId(organizationPublicId);

        return ResponseEntity.ok(response);
    }


}
