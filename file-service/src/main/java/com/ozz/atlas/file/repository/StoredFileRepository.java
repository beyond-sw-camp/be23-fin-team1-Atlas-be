package com.ozz.atlas.file.repository;

import com.ozz.atlas.file.domain.StoredFile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    Optional<StoredFile> findByPublicId(String publicId);
}
