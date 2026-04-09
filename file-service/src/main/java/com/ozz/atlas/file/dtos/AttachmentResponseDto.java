package com.ozz.atlas.file.dtos;

import com.ozz.atlas.file.domain.RefType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponseDto {
    private String attachmentPublicId;
    private RefType refType;
    private String refPublicId;
    private String uploadedByUserPublicId;

    @Builder.Default
    private List<FileResponseDto> files = new ArrayList<>();
}
