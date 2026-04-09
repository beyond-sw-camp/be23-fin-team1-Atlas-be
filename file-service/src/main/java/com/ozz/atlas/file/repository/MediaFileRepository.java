package com.ozz.atlas.file.repository;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.file.domain.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    List<MediaFile> findByAttachmentIdAndStatusOrderBySortOrderAsc(Long attachmentId, Status status);

    Optional<MediaFile> findByPublicIdAndAttachmentIdAndStatus(String publicId, Long attachmentId, Status status);
}
