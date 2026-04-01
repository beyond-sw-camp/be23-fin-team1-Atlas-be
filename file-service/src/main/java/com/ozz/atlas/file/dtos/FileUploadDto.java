package com.ozz.atlas.file.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileUploadDto {
    private String originalFileName;
    private Long size;
    private String mimeType;
    private String uploadedByUserPublicId;
}
