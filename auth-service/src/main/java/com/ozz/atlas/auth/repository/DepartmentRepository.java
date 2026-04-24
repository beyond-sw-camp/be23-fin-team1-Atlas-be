package com.ozz.atlas.auth.repository;

import com.ozz.atlas.auth.domain.Department;
import com.ozz.atlas.common.jpa.Status;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByPublicId(String publicId);

    Optional<Department> findByDepartmentCode(String departmentCode);

    boolean existsByDepartmentCode(String departmentCode);

    boolean existsByDepartmentCodeAndDepartmentIdNot(String departmentCode, Long departmentId);

    List<Department> findAllByStatusOrderByDepartmentNameAsc(Status status);

    List<Department> findAllByOrderByDepartmentNameAsc();
}
