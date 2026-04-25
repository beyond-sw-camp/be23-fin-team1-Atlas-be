package com.ozz.atlas.auth.controller;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.department.DepartmentCreateDto;
import com.ozz.atlas.auth.dtos.department.DepartmentResponseDto;
import com.ozz.atlas.auth.dtos.department.DepartmentStatusUpdateDto;
import com.ozz.atlas.auth.dtos.department.DepartmentUpdateDto;
import com.ozz.atlas.auth.service.DepartmentService;
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
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponseDto>> getActiveDepartments() {
        return ResponseEntity.ok(departmentService.getActiveDepartments());
    }

    @GetMapping("/admin/departments")
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        validateAdmin(principal);
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @PostMapping("/admin/departments")
    public ResponseEntity<DepartmentResponseDto> createDepartment(
            @RequestBody @Valid DepartmentCreateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        validateAdmin(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(dto));
    }

    @PatchMapping("/admin/departments/{departmentPublicId}")
    public ResponseEntity<DepartmentResponseDto> updateDepartment(
            @PathVariable String departmentPublicId,
            @RequestBody @Valid DepartmentUpdateDto dto,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        validateAdmin(principal);
        return ResponseEntity.ok(departmentService.updateDepartment(departmentPublicId, dto));
    }

    @PatchMapping("/admin/departments/{departmentPublicId}/status")
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
