package com.ozz.atlas.file.repository;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.file.domain.DocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {
    List<DocumentFile> findByAttachmentIdAndStatusOrderBySortOrderAsc(Long attachmentId, Status status);

    Optional<DocumentFile> findByPublicIdAndAttachmentIdAndStatus(String publicId, Long attachmentId, Status status);
}
