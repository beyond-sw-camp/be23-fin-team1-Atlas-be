package com.ozz.atlas.file.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateAttachmentFileOrderRequestDto {
    private List<UpdateAttachmentFileOrderItemDto> files = new ArrayList<>();
}
