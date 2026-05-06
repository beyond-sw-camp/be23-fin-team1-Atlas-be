package com.ozz.atlas.supply.item.client;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class FileAttachmentResponse {

    private String attachmentPublicId;
    private String refType;
    private String refPublicId;
    private List<FileMetadataResponse> files = new ArrayList<>();
}
