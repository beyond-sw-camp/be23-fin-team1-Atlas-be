package com.ozz.atlas.file.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAttachmentFileRequestDto {

    // KEEP, DELETE일 때 사용
    private String filePublicId;

    // ADD일 때 사용
    // multipart files 파트의 0-based 순번과 매핑
    private Integer uploadIndex;

    // KEEP, ADD일 때 사용
    // attachment 내 최종 파일 순서
    private Integer sortOrder;

    // KEEP: 기존 파일 유지 및 정렬 변경
    // DELETE: 기존 파일 삭제
    // ADD: multipart files 파트의 새 파일 추가
    private AttachmentFileUpdateAction action;
}
