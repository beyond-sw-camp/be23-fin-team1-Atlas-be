package com.ozz.atlas.file.service;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.file.domain.FileType;
import com.ozz.atlas.file.domain.RefType;
import org.springframework.stereotype.Component;

@Component
public class FileKeyGenerator {

    public String generateStoredFileName(String originalFileName) {
        return PublicIdGenerator.next() + extractExtension(originalFileName);
    }

    public String generateObjectKey(FileType fileType, RefType refType, String userPublicId, String storedFileName) {
        return String.format("%s/%s/%s/%s",
                directoryName(fileType),
                refType.name().toLowerCase(),
                userPublicId,
                storedFileName);
    }

    public String generateThumbnailKey(String objectKey) {
        int dotIndex = objectKey.lastIndexOf('.');
        String base = dotIndex >= 0 ? objectKey.substring(0, dotIndex) : objectKey;
        return base + "_thumb.png";
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null) {
            return "";
        }

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFileName.length() - 1) {
            return "";
        }
        return originalFileName.substring(dotIndex);
    }

    private String directoryName(FileType fileType) {
        return switch (fileType) {
            case MEDIA_FILE -> "media";
            case DOCUMENT_FILE -> "document";
        };
    }
}
