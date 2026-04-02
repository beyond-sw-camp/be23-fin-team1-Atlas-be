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
public class MediaFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_file_id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 26, updatable = false)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 255)
    private String filePath;

    @Column(nullable = false, length = 255)
    private String fileThumbPath;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false, length = 26, updatable = false)
    private String uploadedByUserPublicId;

    @Column(nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    public void deleteMediaFile() {
        this.status = Status.DELETE;
    }

}
