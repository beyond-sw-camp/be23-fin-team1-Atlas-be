package com.ozz.atlas.auth.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.Status;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Organization extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long organizationId;

    @Column(nullable = false, unique = true, length = 26, updatable = false)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrganizationType organizationType;

    @Column(nullable = false, length = 100)
    private String organizationName;

    @Column(length = 30)
    private String businessNo;

    @Column(nullable = false, length = 10)
    private String contactFirstName;

    private String contactMiddleName;

    @Column(nullable = false, length = 10)
    private String contactLastName;

    @Column(length = 100)
    private String contactEmail;

    @Column(nullable = false, length = 30)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;



}
