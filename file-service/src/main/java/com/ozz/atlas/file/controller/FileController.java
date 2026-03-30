package com.ozz.atlas.file.controller;

import com.ozz.atlas.file.dto.request.FileCreateRequest;
import com.ozz.atlas.file.dto.response.FileResponse;
import com.ozz.atlas.file.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<FileResponse> create(@Valid @RequestBody FileCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.create(request));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<FileResponse> get(@PathVariable String publicId) {
        return ResponseEntity.ok(fileService.getByPublicId(publicId));
    }
}
