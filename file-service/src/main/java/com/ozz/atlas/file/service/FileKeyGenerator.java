package com.ozz.atlas.file.service;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.file.domain.FileType;
import com.ozz.atlas.file.domain.RefType;

public class FileKeyGenerator {

    public String keyGenerator(FileType fileType, String userPublicId, RefType refType) {
        String ulid = PublicIdGenerator.next();
        // S3 업로드 될 때 경로 ex) media/profileImage/user public id(ULID)/file ULID
        return String.format("%s/%s/%s/%s", fileType, refType, userPublicId, ulid);
    }
}
