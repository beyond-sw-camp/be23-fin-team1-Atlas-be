package com.ozz.atlas.file.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "stored_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 26, updatable = false)
    private String publicId = PublicIdGenerator.next();

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false, length = 255)
    private String storageKey;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileStatus status = FileStatus.PENDING;

    @Builder
    public StoredFile(String originalName, String storageKey, String contentType, Long size, FileStatus status) {
        this.originalName = originalName;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.size = size;
        this.status = status == null ? FileStatus.PENDING : status;
    }
}
