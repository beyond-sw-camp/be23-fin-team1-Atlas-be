package com.ozz.atlas.file.service;

import com.ozz.atlas.file.domain.FileStatus;
import com.ozz.atlas.file.domain.StoredFile;
import com.ozz.atlas.file.dto.request.FileCreateRequest;
import com.ozz.atlas.file.dto.response.FileResponse;
import com.ozz.atlas.file.repository.StoredFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileService {

    private final StoredFileRepository storedFileRepository;

    public FileService(StoredFileRepository storedFileRepository) {
        this.storedFileRepository = storedFileRepository;
    }

    @Transactional
    public FileResponse create(FileCreateRequest request) {
        StoredFile storedFile = StoredFile.builder()
                .originalName(request.originalName())
                .storageKey(request.storageKey())
                .contentType(request.contentType())
                .size(request.size())
                .status(FileStatus.PENDING)
                .build();

        return FileResponse.from(storedFileRepository.save(storedFile));
    }

    public FileResponse getByPublicId(String publicId) {
        StoredFile storedFile = storedFileRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."));

        return FileResponse.from(storedFile);
    }
}
