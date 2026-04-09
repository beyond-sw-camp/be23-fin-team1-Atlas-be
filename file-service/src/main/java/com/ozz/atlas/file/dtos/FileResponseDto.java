package com.ozz.atlas.file.dtos;

import com.ozz.atlas.file.domain.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponseDto {
    private String attachmentPublicId;
    private String filePublicId;
    private FileType fileType;
    private String originalFileName;
    private String fileName;
    private String filePath;
    private String fileThumbPath;
    private Long size;
    private String mimeType;
    private Integer sortOrder;
    private String uploadedByUserPublicId;
}
