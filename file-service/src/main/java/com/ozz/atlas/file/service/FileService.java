package com.ozz.atlas.file.service;

import com.ozz.atlas.file.dto.request.FileCreateRequest;
import com.ozz.atlas.file.dto.response.FileResponse;

public interface FileService {

    FileResponse create(FileCreateRequest request);

    FileResponse getByPublicId(String publicId);
}
