package com.ozz.atlas.file.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "날짜 요청")
public class UpdateAttachmentFileOrderRequestDto {
    @Schema(description = "files 값", nullable = true)
    private List<UpdateAttachmentFileOrderItemDto> files;
}
