package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.domain.Department;
import com.ozz.atlas.auth.dtos.DepartmentCreateDto;
import com.ozz.atlas.auth.dtos.DepartmentResponseDto;
import com.ozz.atlas.auth.dtos.DepartmentStatusUpdateDto;
import com.ozz.atlas.auth.dtos.DepartmentUpdateDto;
import com.ozz.atlas.auth.repository.DepartmentRepository;
import com.ozz.atlas.common.jpa.Status;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentResponseDto> getActiveDepartments() {
        return departmentRepository.findAllByStatusOrderByDepartmentNameAsc(Status.ACTIVE).stream()
                .map(DepartmentResponseDto::fromEntity)
                .toList();
    }

    public List<DepartmentResponseDto> getAllDepartments() {
        return departmentRepository.findAllByOrderByDepartmentNameAsc().stream()
                .map(DepartmentResponseDto::fromEntity)
                .toList();
    }

    @Transactional
    public DepartmentResponseDto createDepartment(DepartmentCreateDto dto) {
        if (departmentRepository.existsByDepartmentCode(dto.getDepartmentCode())) {
            throw new IllegalArgumentException("이미 존재하는 부서 코드입니다.");
        }

        Department department = Department.create(dto.getDepartmentCode(), dto.getDepartmentName());
        return DepartmentResponseDto.fromEntity(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponseDto updateDepartment(String departmentPublicId, DepartmentUpdateDto dto) {
        Department department = getDepartmentEntity(departmentPublicId);
        department.update(dto.getDepartmentName());
        return DepartmentResponseDto.fromEntity(department);
    }

    @Transactional
    public DepartmentResponseDto updateDepartmentStatus(String departmentPublicId, DepartmentStatusUpdateDto dto) {
        Department department = getDepartmentEntity(departmentPublicId);
        department.changeStatus(dto.getStatus());
        return DepartmentResponseDto.fromEntity(department);
    }

    public Department getActiveDepartmentEntity(String departmentPublicId) {
        Department department = getDepartmentEntity(departmentPublicId);
        if (department.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("비활성화된 부서는 사용자에게 할당할 수 없습니다.");
        }
        return department;
    }

    private Department getDepartmentEntity(String departmentPublicId) {
        return departmentRepository.findByPublicId(departmentPublicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서입니다."));
    }
}
