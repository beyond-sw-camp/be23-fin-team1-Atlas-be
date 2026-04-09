package com.ozz.atlas.file.service;

import com.ozz.atlas.file.dtos.FileUploadReqDto;
import com.ozz.atlas.file.dtos.GetFileDetailDto;
import com.ozz.atlas.file.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public FileService(FileRepository fileRepository, RestTemplate restTemplate) {
        this.fileRepository = fileRepository;
        this.restTemplate = restTemplate;
    }

    // 작업 할 것
    public FileUploadReqDto fileUpload(List<MultipartFile> files, String publicUserId) {
        return null;
    }

    // 작업 할 것
    public GetFileDetailDto getByPublicId(String publicId) {
//        String organizationInfoGetUrl = "http://auth-service/api/auth/organizations/" + publicId;
//        restTemplate.exchange(organizationInfoGetUrl, HttpMethod.GET, null, String.class);
        return null;
    }
}
