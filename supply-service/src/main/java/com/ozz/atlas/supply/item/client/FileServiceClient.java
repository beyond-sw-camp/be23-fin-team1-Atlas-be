package com.ozz.atlas.supply.item.client;

import com.ozz.atlas.supply.item.exception.ItemErrorCode;
import com.ozz.atlas.supply.item.exception.ItemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class FileServiceClient {

    private static final String ITEM_REF_TYPE = "ITEM";

    private final RestTemplate restTemplate;
    private final String fileServiceBaseUrl;

    public FileServiceClient(
            RestTemplate restTemplate,
            @Value("${atlas.file-service.base-url}") String fileServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.fileServiceBaseUrl = fileServiceBaseUrl;
    }

    public FileMetadataResponse getItemImageFile(String itemPublicId, String filePublicId) {
        FileAttachmentResponse attachment = getItemAttachment(itemPublicId);
        if (attachment.getFiles() == null) {
            throw new ItemException(ItemErrorCode.ITEM_MEDIA_NOT_FOUND);
        }

        FileMetadataResponse file = attachment.getFiles().stream()
                .filter(candidate -> filePublicId.equals(candidate.getFilePublicId()))
                .findFirst()
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_MEDIA_NOT_FOUND));

        if (!isImage(file)) {
            throw new ItemException(ItemErrorCode.ITEM_MEDIA_NOT_IMAGE);
        }
        return file;
    }

    private FileAttachmentResponse getItemAttachment(String itemPublicId) {
        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(fileServiceBaseUrl)
                    .path("/api/files/attachments/by-ref")
                    .queryParam("refType", ITEM_REF_TYPE)
                    .queryParam("refPublicId", itemPublicId)
                    .build()
                    .toUri();

            FileAttachmentResponse response = restTemplate.getForObject(uri, FileAttachmentResponse.class);
            if (response == null || !ITEM_REF_TYPE.equals(response.getRefType()) || !itemPublicId.equals(response.getRefPublicId())) {
                throw new ItemException(ItemErrorCode.ITEM_MEDIA_NOT_FOUND);
            }
            return response;
        } catch (RestClientException e) {
            throw new ItemException(ItemErrorCode.ITEM_MEDIA_NOT_FOUND);
        }
    }

    private boolean isImage(FileMetadataResponse file) {
        return file.getMimeType() != null && file.getMimeType().startsWith("image/");
    }
}
