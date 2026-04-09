package com.ozz.atlas.file.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAttachmentFileOrderItemDto {
    private String filePublicId;
    private Integer sortOrder;
}
