package com.ozz.atlas.file.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Attachment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id", nullable = false)
    private Long id;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(nullable = false, length = 26, updatable = false)
    private String resourcePublicId; // file public id

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private RefType refType;

    @Column(nullable = false, length = 26, updatable = false)
    private String refPublicId; // reference public id

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 1;
}
