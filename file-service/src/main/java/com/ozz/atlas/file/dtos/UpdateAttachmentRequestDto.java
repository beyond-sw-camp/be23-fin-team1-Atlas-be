package com.ozz.atlas.file.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateAttachmentRequestDto {

    // PATCH /api/files/attachments/{attachmentPublicId}
    // request 파트 예시
    // {
    //   "files": [
    //     { "filePublicId": "01KEEP...", "sortOrder": 1, "action": "KEEP" },
    //     { "filePublicId": "01DELETE...", "action": "DELETE" },
    //     { "uploadIndex": 0, "sortOrder": 2, "action": "ADD" }
    //   ]
    // }
    // 새 파일 본문은 multipart files 파트로 받고, 여기서는 uploadIndex로 매핑
    private List<UpdateAttachmentFileRequestDto> files = new ArrayList<>();
}
