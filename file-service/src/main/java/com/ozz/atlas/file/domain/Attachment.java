package com.ozz.atlas.file.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.jpa.Status;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Attachment extends BaseTimeEntity {
    // 전체 파일의 연결관계 관리

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 26, updatable = false)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private RefType refType;

    @Column(nullable = false, length = 26, updatable = false)
    private String refPublicId; // reference public id

    @Column(nullable = false, length = 26, updatable = false)
    private String uploadedByUserPublicId;

    @Column(nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    public void deleteAttachmentFile() {
        this.status = Status.DELETE;
    }
}
