package com.ozz.atlas.file.controller;

import com.ozz.atlas.file.domain.RefType;
import com.ozz.atlas.file.dtos.AttachmentResponseDto;
import com.ozz.atlas.file.dtos.CreateAttachmentRequestDto;
import com.ozz.atlas.file.dtos.FileResponseDto;
import com.ozz.atlas.file.dtos.UpdateAttachmentFileOrderRequestDto;
import com.ozz.atlas.file.dtos.UpdateAttachmentRequestDto;
import com.ozz.atlas.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 새 Attachment를 생성하고 해당 Attachment 아래에 파일들을 업로드할 때 사용하는 API
    // 예: 아이템 등록, 인증서 등록, 반품 등록 시 첨부 묶음을 처음 생성하는 경우
    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponseDto> createAttachment(
            @RequestPart("request") CreateAttachmentRequestDto request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("X-User-Public-Id") String publicUserId) {
        AttachmentResponseDto res = fileService.createAttachment(request, files, publicUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // 특정 attachmentPublicId로 첨부 묶음과 그 하위 파일 목록을 조회할 때 사용하는 API
    @GetMapping("/attachments/{attachmentPublicId}")
    public ResponseEntity<AttachmentResponseDto> getAttachment(@PathVariable String attachmentPublicId) {
        return ResponseEntity.ok(fileService.getAttachment(attachmentPublicId));
    }

    // 특정 refType, refPublicId에 연결된 attachment를 조회할 때 사용하는 API
    // 예: ITEM 상세 화면에서 해당 아이템의 첨부 파일 묶음을 가져오는 경우
    @GetMapping("/attachments/by-ref")
    public ResponseEntity<AttachmentResponseDto> getAttachmentByRef(@RequestParam RefType refType,
                                                                    @RequestParam String refPublicId) {
        return ResponseEntity.ok(fileService.getAttachmentByRef(refType, refPublicId));
    }

    // 특정 attachment 아래에 속한 file 1건의 메타데이터를 조회할 때 사용하는 API
    // 예: attachment 소속 검증 후 파일 미리보기, 다운로드 전 상세 정보 확인
    @GetMapping("/attachments/{attachmentPublicId}/files/{filePublicId}")
    public ResponseEntity<FileResponseDto> getFile(@PathVariable String attachmentPublicId,
                                                   @PathVariable String filePublicId) {
        return ResponseEntity.ok(fileService.getFile(attachmentPublicId, filePublicId));
    }

    // 기존 attachment에 파일을 추가 업로드할 때 사용하는 API
    // 예: 아이템 수정 화면에서 기존 첨부 묶음에 이미지 몇 장을 더 추가하는 경우
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
    @PatchMapping(value = "/attachments/{attachmentPublicId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponseDto> updateAttachment(@PathVariable String attachmentPublicId,
                                                                  @RequestPart("request") UpdateAttachmentRequestDto request,
                                                                  @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                                  @RequestHeader("X-User-Public-Id") String publicUserId) {
        return ResponseEntity.ok(fileService.updateAttachment(attachmentPublicId, request, files, publicUserId));
    }

    // attachment와 그 하위 파일들을 함께 삭제할 때 사용하는 API
    // 예: 아이템 첨부 전체를 제거하거나 인증서 첨부 묶음을 통째로 삭제하는 경우
    @DeleteMapping("/attachments/{attachmentPublicId}")
    public ResponseEntity<String> deleteAttachment(@PathVariable String attachmentPublicId) {
        fileService.deleteAttachment(attachmentPublicId);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    // 빠른 조작용
    //

    // 특정 attachment에 속한 파일 1건만 삭제할 때 사용하는 API
    // 예: 첨부 묶음은 유지하고 특정 이미지 1장만 제거하는 경우
    @DeleteMapping("/attachments/{attachmentPublicId}/files/{filePublicId}")
    public ResponseEntity<String> deleteFile(@PathVariable String attachmentPublicId,
                                             @PathVariable String filePublicId) {
        fileService.deleteFile(attachmentPublicId, filePublicId);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    // 파일 순서(sortOrder) 빠르게 변경
    // 예: 아이템의 이미지 리스트에서 한 이미지를 드래그해서 앞으로 옮겨 순서를 변경
    @PatchMapping("/attachments/{attachmentPublicId}/files/order")
    public ResponseEntity<AttachmentResponseDto> updateFileOrder(@PathVariable String attachmentPublicId,
                                                                 @RequestBody UpdateAttachmentFileOrderRequestDto request) {
        return ResponseEntity.ok(fileService.updateFileOrder(attachmentPublicId, request));
    }

}
