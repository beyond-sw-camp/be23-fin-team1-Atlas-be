package com.ozz.atlas.file.repository;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.file.domain.Attachment;
import com.ozz.atlas.file.domain.RefType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Optional<Attachment> findByPublicIdAndStatus(String publicId, Status status);

    Optional<Attachment> findByRefTypeAndRefPublicIdAndStatus(RefType refType, String refPublicId, Status status);

    boolean existsByRefTypeAndRefPublicIdAndStatus(RefType refType, String refPublicId, Status status);
}
