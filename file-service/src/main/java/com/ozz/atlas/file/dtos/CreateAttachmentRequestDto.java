package com.ozz.atlas.file.dtos;

import com.ozz.atlas.file.domain.RefType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAttachmentRequestDto {
    private RefType refType;
    private String refPublicId;
}
