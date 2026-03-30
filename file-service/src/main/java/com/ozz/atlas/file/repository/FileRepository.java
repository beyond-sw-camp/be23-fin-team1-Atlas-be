package com.ozz.atlas.file.repository;

import java.util.Optional;

import com.ozz.atlas.file.domain.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<MediaFile, Long> {

    Optional<MediaFile> findByPublicId(String publicId);
}
