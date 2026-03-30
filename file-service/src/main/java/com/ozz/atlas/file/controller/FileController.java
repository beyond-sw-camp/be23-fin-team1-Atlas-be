package com.ozz.atlas.file.controller;

import com.ozz.atlas.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

//    @PostMapping
//    public ResponseEntity<FileResponse> create(@Valid @RequestBody FileCreateRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.create(request));
//    }
//
//    @GetMapping("/{publicId}")
//    public ResponseEntity<FileResponse> get(@PathVariable String publicId) {
//        return ResponseEntity.ok(fileService.getByPublicId(publicId));
//    }
}
