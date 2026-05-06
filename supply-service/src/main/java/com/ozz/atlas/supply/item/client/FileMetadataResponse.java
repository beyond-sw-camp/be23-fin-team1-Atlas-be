package com.ozz.atlas.supply.item.client;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileMetadataResponse {

    private String filePublicId;
    private String filePath;
    private String fileThumbPath;
    private String mimeType;
}
