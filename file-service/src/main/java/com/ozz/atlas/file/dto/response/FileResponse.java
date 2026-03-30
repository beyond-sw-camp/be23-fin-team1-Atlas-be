package com.ozz.atlas.file.dto.response;

import com.ozz.atlas.file.domain.FileStatus;
import com.ozz.atlas.file.domain.StoredFile;

public record FileResponse(
        String publicId,
        String originalName,
        String storageKey,
        String contentType,
        Long size,
        FileStatus status
) {

    public static FileResponse from(StoredFile storedFile) {
        return new FileResponse(
                storedFile.getPublicId(),
                storedFile.getOriginalName(),
                storedFile.getStorageKey(),
                storedFile.getContentType(),
                storedFile.getSize(),
                storedFile.getStatus()
        );
    }
}
