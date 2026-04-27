package com.ozz.atlas.file.controller;

import com.ozz.atlas.file.domain.RefType;
import com.ozz.atlas.file.dtos.AttachmentResponseDto;
import com.ozz.atlas.file.dtos.CreateAttachmentRequestDto;
import com.ozz.atlas.file.dtos.FileResponseDto;
import com.ozz.atlas.file.dtos.UpdateAttachmentFileOrderRequestDto;
import com.ozz.atlas.file.dtos.UpdateAttachmentRequestDto;
import com.ozz.atlas.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 새 Attachment를 생성하고 해당 Attachment 아래에 파일들을 업로드할 때 사용하는 API
    // 예: 아이템 등록, 인증서 등록, 반품 등록 시 첨부 묶음을 처음 생성하는 경우
    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "첨부 묶음 생성",
            description = "메타데이터와 파일들을 함께 업로드해 attachment를 생성한다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    description = "request 파트는 CreateAttachmentRequestDto JSON, files 파트는 업로드할 바이너리 파일 배열"
                            ),
                            encoding = {
                                    @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "files", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            },
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "request": {
                                                "refType": "ITEM",
                                                "refPublicId": "item_01HZY2ITEM123456789"
                                              },
                                              "files": [
                                                "(binary file)",
                                                "(binary file)"
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            responses = @ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = AttachmentResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "attachmentPublicId": "att_01HZY3ATT123456789",
                                              "refType": "ITEM",
                                              "refPublicId": "item_01HZY2ITEM123456789",
                                              "uploadedByUserPublicId": "usr_01HZXA1B2C3D4E5F6G7H8J9K0",
                                              "files": [
                                                {
                                                  "attachmentPublicId": "att_01HZY3ATT123456789",
                                                  "filePublicId": "file_01HZY3FILE123456789",
                                                  "fileType": "IMAGE",
                                                  "originalFileName": "invoice-april.png",
                                                  "fileName": "01HZY3FILE123456789.png",
                                                  "filePath": "https://cdn.atlas.com/files/01HZY3FILE123456789.png",
                                                  "fileThumbPath": "https://cdn.atlas.com/files/thumbs/01HZY3FILE123456789.png",
                                                  "size": 248120,
                                                  "mimeType": "image/png",
                                                  "sortOrder": 1,
                                                  "uploadedByUserPublicId": "usr_01HZXA1B2C3D4E5F6G7H8J9K0"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<AttachmentResponseDto> createAttachment(
            @RequestPart("request") CreateAttachmentRequestDto request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("X-User-Public-Id") String publicUserId) {
        AttachmentResponseDto res = fileService.createAttachment(request, files, publicUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // 특정 attachmentPublicId로 첨부 묶음과 그 하위 파일 목록을 조회할 때 사용하는 API
    @Operation(summary = "첨부 묶음 상세 조회")
    @GetMapping("/attachments/{attachmentPublicId}")
    public ResponseEntity<AttachmentResponseDto> getAttachment(@PathVariable String attachmentPublicId) {
        return ResponseEntity.ok(fileService.getAttachment(attachmentPublicId));
    }

    // 특정 refType, refPublicId에 연결된 attachment를 조회할 때 사용하는 API
    // 예: ITEM 상세 화면에서 해당 아이템의 첨부 파일 묶음을 가져오는 경우
    @Operation(summary = "참조 기준 첨부 묶음 조회")
    @GetMapping("/attachments/by-ref")
    public ResponseEntity<AttachmentResponseDto> getAttachmentByRef(@RequestParam RefType refType,
                                                                    @RequestParam String refPublicId) {
        return ResponseEntity.ok(fileService.getAttachmentByRef(refType, refPublicId));
    }

    // 특정 attachment 아래에 속한 file 1건의 메타데이터를 조회할 때 사용하는 API
    // 예: attachment 소속 검증 후 파일 미리보기, 다운로드 전 상세 정보 확인
    @Operation(summary = "첨부 파일 상세 조회")
    @GetMapping("/attachments/{attachmentPublicId}/files/{filePublicId}")
    public ResponseEntity<FileResponseDto> getFile(@PathVariable String attachmentPublicId,
                                                   @PathVariable String filePublicId) {
        return ResponseEntity.ok(fileService.getFile(attachmentPublicId, filePublicId));
    }

    // 기존 attachment에 파일을 추가 업로드할 때 사용하는 API
    // 예: 아이템 수정 화면에서 기존 첨부 묶음에 이미지 몇 장을 더 추가하는 경우
    @Operation(summary = "첨부 파일 추가")
    @PostMapping(value = "/attachments/{attachmentPublicId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponseDto> appendFiles(@PathVariable String attachmentPublicId,
                                                             @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                             @RequestHeader("X-User-Public-Id") String publicUserId) {
        return ResponseEntity.ok(fileService.appendFiles(attachmentPublicId, files, publicUserId));
    }

    // attachment 하위 파일 목록을 수정할 때 사용하는 API
    // 예: 기존 파일 일부 삭제, 기존 파일 순서 변경, 새 파일 추가 업로드를 한 번에 처리하는 경우
    // request 파트는 application/json 형식으로 보내고, files 파트는 새로 추가할 파일 본문만 순서대로 보낸다.
    // request.files[].uploadIndex 는 multipart files 파트의 0-based 순번과 매핑한다.
    // action 규칙:
    // KEEP -> filePublicId, sortOrder 필요
    // DELETE -> filePublicId 필요
    // ADD -> uploadIndex 필요, sortOrder 권장
    // attachment 전체 삭제는 DELETE /attachments/{attachmentPublicId} 로 분리
    @Operation(summary = "첨부 묶음 수정")
    @PatchMapping(value = "/attachments/{attachmentPublicId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponseDto> updateAttachment(@PathVariable String attachmentPublicId,
                                                                  @RequestPart("request") UpdateAttachmentRequestDto request,
                                                                  @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                                  @RequestHeader("X-User-Public-Id") String publicUserId) {
        return ResponseEntity.ok(fileService.updateAttachment(attachmentPublicId, request, files, publicUserId));
    }

    // attachment와 그 하위 파일들을 함께 삭제할 때 사용하는 API
    // 예: 아이템 첨부 전체를 제거하거나 인증서 첨부 묶음을 통째로 삭제하는 경우
    @Operation(summary = "첨부 묶음 삭제")
    @DeleteMapping("/attachments/{attachmentPublicId}")
    public ResponseEntity<String> deleteAttachment(@PathVariable String attachmentPublicId) {
        fileService.deleteAttachment(attachmentPublicId);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    // 빠른 조작용
    //

    // 특정 attachment에 속한 파일 1건만 삭제할 때 사용하는 API
    // 예: 첨부 묶음은 유지하고 특정 이미지 1장만 제거하는 경우
    @Operation(summary = "첨부 파일 삭제")
    @DeleteMapping("/attachments/{attachmentPublicId}/files/{filePublicId}")
    public ResponseEntity<String> deleteFile(@PathVariable String attachmentPublicId,
                                             @PathVariable String filePublicId) {
        fileService.deleteFile(attachmentPublicId, filePublicId);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    // 파일 순서(sortOrder) 빠르게 변경
    // 예: 아이템의 이미지 리스트에서 한 이미지를 드래그해서 앞으로 옮겨 순서를 변경
    @Operation(summary = "첨부 파일 순서 변경")
    @PatchMapping("/attachments/{attachmentPublicId}/files/order")
    public ResponseEntity<AttachmentResponseDto> updateFileOrder(@PathVariable String attachmentPublicId,
                                                                 @RequestBody UpdateAttachmentFileOrderRequestDto request) {
        return ResponseEntity.ok(fileService.updateFileOrder(attachmentPublicId, request));
    }

}
