package com.ozz.atlas.file.controller;

import com.ozz.atlas.file.dtos.FileUploadReqDto;
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

    // CRUD

    // file upload
    @PostMapping(value = "/fileUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart(required = false) List<MultipartFile> files,
                                    @RequestHeader("X-User-Public-Id") String publicUserId) {
        FileUploadReqDto res = fileService.fileUpload(files, publicUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // file detail info
    @GetMapping(value = "/{publicId}")
    public ResponseEntity<?> getFileInfo( String publicId) {
        return ResponseEntity.ok(fileService.getByPublicId(publicId));
    }
    // file edit(attachment edit -> attachment 일부 수정 시)

}
