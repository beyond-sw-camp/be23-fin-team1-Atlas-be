package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.department.DepartmentCreateDto;
import com.ozz.atlas.auth.dtos.department.DepartmentResponseDto;
import com.ozz.atlas.auth.dtos.department.DepartmentStatusUpdateDto;
import com.ozz.atlas.auth.dtos.department.DepartmentUpdateDto;
import com.ozz.atlas.auth.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Department")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/departments")
    @Operation(
            summary = "활성 부서 목록 조회",
            description = "직원 생성/수정 화면의 부서 드롭다운에 사용할 활성 부서 목록을 조회한다."
    )
    public ResponseEntity<List<DepartmentResponseDto>> getActiveDepartments() {
        return ResponseEntity.ok(departmentService.getActiveDepartments());
    }

    @GetMapping("/admin/departments")
    @Operation(
            summary = "부서 전체 목록 조회",
            description = "플랫폼 관리자가 활성/비활성 부서를 포함한 전체 부서 마스터 목록을 조회한다."
    )
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        validateAdmin(principal);
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @PostMapping("/admin/departments")
    @Operation(
            summary = "부서 생성",
            description = "플랫폼 관리자가 조직 사용자에게 할당할 부서 마스터를 생성한다."
    )
    public ResponseEntity<DepartmentResponseDto> createDepartment(
            @RequestBody @Valid DepartmentCreateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        validateAdmin(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(dto));
    }

    @PatchMapping("/admin/departments/{departmentPublicId}")
    @Operation(
            summary = "부서 수정",
            description = "플랫폼 관리자가 부서명을 수정한다."
    )
    public ResponseEntity<DepartmentResponseDto> updateDepartment(
            @PathVariable String departmentPublicId,
            @RequestBody @Valid DepartmentUpdateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        validateAdmin(principal);
        return ResponseEntity.ok(departmentService.updateDepartment(departmentPublicId, dto));
    }

    @PatchMapping("/admin/departments/{departmentPublicId}/status")
    @Operation(
            summary = "부서 상태 변경",
            description = "플랫폼 관리자가 부서의 활성/비활성 상태를 변경한다."
    )
    public ResponseEntity<DepartmentResponseDto> updateDepartmentStatus(
            @PathVariable String departmentPublicId,
            @RequestBody @Valid DepartmentStatusUpdateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        validateAdmin(principal);
        return ResponseEntity.ok(departmentService.updateDepartmentStatus(departmentPublicId, dto));
    }

    private void validateAdmin(AuthPrincipal principal) {
        if (principal == null || principal.role() != UserRole.ADMIN) {
            throw new IllegalArgumentException("부서 관리 권한이 없습니다.");
        }
    }
}
