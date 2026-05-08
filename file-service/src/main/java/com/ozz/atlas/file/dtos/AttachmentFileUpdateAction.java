package com.ozz.atlas.file.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "날짜 모델")
public enum AttachmentFileUpdateAction {
    KEEP,
    DELETE,
    ADD
}
