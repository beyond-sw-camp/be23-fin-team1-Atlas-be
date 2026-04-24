package com.ozz.atlas.auth.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.jpa.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "department")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    @Column(nullable = false, unique = true, length = 26, updatable = false)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(nullable = false, unique = true, length = 50)
    private String departmentCode;

    @Column(nullable = false, length = 100)
    private String departmentName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    public static Department create(String departmentCode, String departmentName) {
        return Department.builder()
                .departmentCode(departmentCode)
                .departmentName(departmentName)
                .status(Status.ACTIVE)
                .build();
    }

    public void update(String departmentName) {
        this.departmentName = departmentName;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }
}
