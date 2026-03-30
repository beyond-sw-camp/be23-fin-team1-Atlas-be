package com.ozz.atlas.file.service;

import com.ozz.atlas.file.domain.FileStatus;
import com.ozz.atlas.file.dto.request.FileCreateRequest;
import com.ozz.atlas.file.dto.response.FileResponse;
import com.ozz.atlas.file.domain.StoredFile;
import com.ozz.atlas.file.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService {

    private final StoredFileRepository storedFileRepository;

    @Override
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

    @Override
    public FileResponse getByPublicId(String publicId) {
        StoredFile storedFile = storedFileRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "file not found"));

        return FileResponse.from(storedFile);
    }
}
