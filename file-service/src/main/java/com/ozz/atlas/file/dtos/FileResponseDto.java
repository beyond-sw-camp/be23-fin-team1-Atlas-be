package com.ozz.atlas.file.dtos;

import com.ozz.atlas.file.domain.FileType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "첨부 파일 메타데이터 응답")
public class FileResponseDto {
    @Schema(description = "상위 attachment 공개 식별자", example = "att_01HZY3ATT123456789")
    private String attachmentPublicId;
    @Schema(description = "파일 공개 식별자", example = "file_01HZY3FILE123456789")
    private String filePublicId;
    @Schema(description = "파일 타입", example = "IMAGE")
    private FileType fileType;
    @Schema(description = "원본 파일명", example = "invoice-april.png")
    private String originalFileName;
    @Schema(description = "저장된 파일명", example = "01HZY3FILE123456789.png")
    private String fileName;
    @Schema(description = "원본 파일 경로", example = "https://cdn.atlas.com/files/01HZY3FILE123456789.png")
    private String filePath;
    @Schema(description = "썸네일 파일 경로", example = "https://cdn.atlas.com/files/thumbs/01HZY3FILE123456789.png", nullable = true)
    private String fileThumbPath;
    @Schema(description = "파일 크기(byte)", example = "248120")
    private Long size;
    @Schema(description = "MIME 타입", example = "image/png")
    private String mimeType;
    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder;
    @Schema(description = "업로드 사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String uploadedByUserPublicId;
}
