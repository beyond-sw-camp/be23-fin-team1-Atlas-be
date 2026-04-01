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
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(nullable = false, unique = true, length = 26)
    private String publicId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    @Column(nullable = false, unique = true, length = 50)
    private String loginId;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false, length = 50)
    private String userName;
    @Column(nullable = false, length = 100)
    private String email;
    @Column(nullable = false, length = 30)
    private String phone;
    @Column(length = 50)
    private String jobTitle;
    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", nullable = false, length = 40)
    private UserRole userRole;
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
