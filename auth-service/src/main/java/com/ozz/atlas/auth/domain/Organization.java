package com.ozz.atlas.auth.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.id.PublicIdGenerator;
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
    @Column(nullable = false, unique = true, length = 26)
    private String publicId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrganizationType organizationType;
    @Column(nullable = false, length = 100)
    private String organizationName;
    @Column(length = 30)
    private String businessNo;
    @Column(nullable = false, length = 50)
    private String contactName;
    @Column(length = 100)
    private String contactEmail;
    @Column(nullable = false, length = 30)
    private String contactPhone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = PublicIdGenerator.next();
        }
    }

}
