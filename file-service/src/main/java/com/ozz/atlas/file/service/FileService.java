package com.ozz.atlas.file.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.file.domain.Attachment;
import com.ozz.atlas.file.domain.DocumentFile;
import com.ozz.atlas.file.domain.FileType;
import com.ozz.atlas.file.domain.MediaFile;
import com.ozz.atlas.file.domain.RefType;
import com.ozz.atlas.file.dtos.AttachmentResponseDto;
import com.ozz.atlas.file.dtos.AttachmentFileUpdateAction;
import com.ozz.atlas.file.dtos.CreateAttachmentRequestDto;
import com.ozz.atlas.file.dtos.FileResponseDto;
import com.ozz.atlas.file.dtos.UpdateAttachmentFileOrderItemDto;
import com.ozz.atlas.file.dtos.UpdateAttachmentFileOrderRequestDto;
import com.ozz.atlas.file.dtos.UpdateAttachmentFileRequestDto;
import com.ozz.atlas.file.dtos.UpdateAttachmentRequestDto;
import com.ozz.atlas.file.exception.FileErrorCode;
import com.ozz.atlas.file.exception.FileException;
import com.ozz.atlas.file.repository.AttachmentRepository;
import com.ozz.atlas.file.repository.DocumentFileRepository;
import com.ozz.atlas.file.repository.MediaFileRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class FileService {

    private final AttachmentRepository attachmentRepository;
    private final MediaFileRepository mediaFileRepository;
    private final DocumentFileRepository documentFileRepository;
    private final S3StorageService s3StorageService;
    private final FileKeyGenerator fileKeyGenerator;

    @Value("${app.file.s3.region:ap-northeast-2}")
    private String s3Region;

    @Value("${app.file.s3.media-bucket:}")
    private String mediaBucket;

    @Value("${app.file.s3.document-bucket:}")
    private String documentBucket;

    public FileService(AttachmentRepository attachmentRepository,
                       MediaFileRepository mediaFileRepository,
                       DocumentFileRepository documentFileRepository,
                       S3StorageService s3StorageService,
                       FileKeyGenerator fileKeyGenerator) {
        this.attachmentRepository = attachmentRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.documentFileRepository = documentFileRepository;
        this.s3StorageService = s3StorageService;
        this.fileKeyGenerator = fileKeyGenerator;
    }

    @Transactional
    public AttachmentResponseDto createAttachment(CreateAttachmentRequestDto request,
                                                  List<MultipartFile> files,
                                                  String publicUserId) {
        validateFilesForCreateOrAppend(files);

        if (attachmentRepository.existsByRefTypeAndRefPublicIdAndStatus(request.getRefType(), request.getRefPublicId(), Status.ACTIVE)) {
            throw new FileException(FileErrorCode.ATTACHMENT_ALREADY_EXISTS);
        }

        Attachment attachment = attachmentRepository.save(Attachment.builder()
                .refType(request.getRefType())
                .refPublicId(request.getRefPublicId())
                .uploadedByUserPublicId(publicUserId)
                .build());

        uploadFiles(attachment, files, publicUserId, null);
        return toAttachmentResponse(attachment);
    }

    public AttachmentResponseDto getAttachment(String attachmentPublicId) {
        return toAttachmentResponse(getActiveAttachment(attachmentPublicId));
    }

    public AttachmentResponseDto getAttachmentByRef(RefType refType, String refPublicId) {
        Attachment attachment = attachmentRepository.findByRefTypeAndRefPublicIdAndStatus(refType, refPublicId, Status.ACTIVE)
                .orElseThrow(() -> new FileException(FileErrorCode.ATTACHMENT_NOT_FOUND));
        return toAttachmentResponse(attachment);
    }

    public FileResponseDto getFile(String attachmentPublicId, String filePublicId) {
        Attachment attachment = getActiveAttachment(attachmentPublicId);
        return findFileResponse(attachment, filePublicId);
    }

    @Transactional
    public AttachmentResponseDto appendFiles(String attachmentPublicId,
                                             List<MultipartFile> files,
                                             String publicUserId) {
        validateFilesForCreateOrAppend(files);

        Attachment attachment = getActiveAttachment(attachmentPublicId);
        uploadFiles(attachment, files, publicUserId, nextSortOrder(attachment.getId()));
        return toAttachmentResponse(attachment);
    }

    @Transactional
    public AttachmentResponseDto updateAttachment(String attachmentPublicId,
                                                  UpdateAttachmentRequestDto request,
                                                  List<MultipartFile> files,
                                                  String publicUserId) {
        Attachment attachment = getActiveAttachment(attachmentPublicId);
        Map<Integer, MultipartFile> uploadFileMap = indexFiles(files);

        for (UpdateAttachmentFileRequestDto fileRequest : request.getFiles()) {
            if (fileRequest.getAction() == null) {
                throw new FileException(FileErrorCode.INVALID_FILE_REQUEST);
            }

            switch (fileRequest.getAction()) {
                case KEEP -> updateExistingFileSortOrder(attachment, fileRequest.getFilePublicId(), fileRequest.getSortOrder());
                case DELETE -> deleteFile(attachmentPublicId, fileRequest.getFilePublicId());
                case ADD -> addSingleFileFromRequest(attachment, fileRequest, uploadFileMap, publicUserId);
            }
        }

        return toAttachmentResponse(attachment);
    }

    @Transactional
    public AttachmentResponseDto updateFileOrder(String attachmentPublicId,
                                                 UpdateAttachmentFileOrderRequestDto request) {
        Attachment attachment = getActiveAttachment(attachmentPublicId);

        for (UpdateAttachmentFileOrderItemDto fileOrder : request.getFiles()) {
            updateExistingFileSortOrder(attachment, fileOrder.getFilePublicId(), fileOrder.getSortOrder());
        }

        return toAttachmentResponse(attachment);
    }

    @Transactional
    public void deleteAttachment(String attachmentPublicId) {
        Attachment attachment = getActiveAttachment(attachmentPublicId);
        attachment.deleteAttachmentFile();

        mediaFileRepository.findByAttachmentIdAndStatusOrderBySortOrderAsc(attachment.getId(), Status.ACTIVE)
                .forEach(MediaFile::deleteMediaFile);
        documentFileRepository.findByAttachmentIdAndStatusOrderBySortOrderAsc(attachment.getId(), Status.ACTIVE)
                .forEach(DocumentFile::deleteDocumentFile);
    }

    @Transactional
    public void deleteFile(String attachmentPublicId, String filePublicId) {
        Attachment attachment = getActiveAttachment(attachmentPublicId);

        Optional<MediaFile> mediaFile = mediaFileRepository.findByPublicIdAndAttachmentIdAndStatus(filePublicId, attachment.getId(), Status.ACTIVE);
        if (mediaFile.isPresent()) {
            mediaFile.get().deleteMediaFile();
            return;
        }

        DocumentFile documentFile = documentFileRepository.findByPublicIdAndAttachmentIdAndStatus(filePublicId, attachment.getId(), Status.ACTIVE)
                .orElseThrow(() -> new FileException(FileErrorCode.FILE_NOT_FOUND));
        documentFile.deleteDocumentFile();
    }

    private Attachment getActiveAttachment(String attachmentPublicId) {
        return attachmentRepository.findByPublicIdAndStatus(attachmentPublicId, Status.ACTIVE)
                .orElseThrow(() -> new FileException(FileErrorCode.ATTACHMENT_NOT_FOUND));
    }

    private void uploadFiles(Attachment attachment,
                             List<MultipartFile> files,
                             String publicUserId,
                             Integer startSortOrder) {
        if (files == null || files.isEmpty()) {
            return;
        }

        int sortOrder = startSortOrder != null ? startSortOrder : 1;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            storeFile(attachment, file, publicUserId, sortOrder++);
        }
    }

    private void validateFilesForCreateOrAppend(List<MultipartFile> files) {
        if (files == null || files.stream().noneMatch(file -> file != null && !file.isEmpty())) {
            throw new FileException(FileErrorCode.INVALID_FILE_REQUEST);
        }
    }

    private void addSingleFileFromRequest(Attachment attachment,
                                          UpdateAttachmentFileRequestDto fileRequest,
                                          Map<Integer, MultipartFile> uploadFileMap,
                                          String publicUserId) {
        if (fileRequest.getUploadIndex() == null) {
            throw new FileException(FileErrorCode.INVALID_FILE_REQUEST);
        }

        MultipartFile uploadFile = uploadFileMap.get(fileRequest.getUploadIndex());
        if (uploadFile == null || uploadFile.isEmpty()) {
            throw new FileException(FileErrorCode.INVALID_FILE_REQUEST);
        }

        int sortOrder = fileRequest.getSortOrder() != null ? fileRequest.getSortOrder() : nextSortOrder(attachment.getId());
        storeFile(attachment, uploadFile, publicUserId, sortOrder);
    }

    private void storeFile(Attachment attachment,
                           MultipartFile file,
                           String publicUserId,
                           Integer sortOrder) {
        try {
            FileType fileType = resolveFileType(file.getContentType());
            String bucket = bucketFor(fileType);
            String storedFileName = fileKeyGenerator.generateStoredFileName(file.getOriginalFilename());
            String objectKey = fileKeyGenerator.generateObjectKey(fileType, attachment.getRefType(), publicUserId, storedFileName);

            s3StorageService.upload(bucket, objectKey, file);

            if (fileType == FileType.MEDIA_FILE) {
                String thumbPath = buildS3Url(bucket, objectKey);
                if (isImage(file.getContentType())) {
                    byte[] thumbnailBytes = createImageThumbnail(file);
                    String thumbnailKey = fileKeyGenerator.generateThumbnailKey(objectKey);
                    s3StorageService.upload(bucket, thumbnailKey, thumbnailBytes, "image/png");
                    thumbPath = buildS3Url(bucket, thumbnailKey);
                }

                mediaFileRepository.save(MediaFile.builder()
                        .attachment(attachment)
                        .originalFileName(defaultFileName(file.getOriginalFilename(), storedFileName))
                        .fileName(storedFileName)
                        .filePath(buildS3Url(bucket, objectKey))
                        .fileThumbPath(thumbPath)
                        .size(file.getSize())
                        .mimeType(file.getContentType())
                        .sortOrder(sortOrder)
                        .uploadedByUserPublicId(publicUserId)
                        .build());
                return;
            }

            documentFileRepository.save(DocumentFile.builder()
                    .attachment(attachment)
                    .originalFileName(defaultFileName(file.getOriginalFilename(), storedFileName))
                    .fileName(storedFileName)
                    .filePath(buildS3Url(bucket, objectKey))
                    .size(file.getSize())
                    .mimeType(file.getContentType())
                    .sortOrder(sortOrder)
                    .uploadedByUserPublicId(publicUserId)
                    .build());
        } catch (IOException e) {
            throw new FileException(FileErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private FileType resolveFileType(String mimeType) {
        if (!StringUtils.hasText(mimeType)) {
            throw new FileException(FileErrorCode.INVALID_FILE_TYPE);
        }

        if (mimeType.startsWith("image/") || mimeType.startsWith("video/") || mimeType.startsWith("audio/")) {
            return FileType.MEDIA_FILE;
        }

        if (mimeType.startsWith("application/") || mimeType.startsWith("text/")) {
            return FileType.DOCUMENT_FILE;
        }

        throw new FileException(FileErrorCode.INVALID_FILE_TYPE);
    }

    private boolean isImage(String mimeType) {
        return StringUtils.hasText(mimeType) && mimeType.startsWith("image/");
    }

    private byte[] createImageThumbnail(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(320, 320)
                .outputFormat("png")
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    private String bucketFor(FileType fileType) {
        String bucket = fileType == FileType.MEDIA_FILE ? mediaBucket : documentBucket;
        if (!StringUtils.hasText(bucket)) {
            throw new FileException(FileErrorCode.FILE_UPLOAD_FAILED);
        }
        return bucket;
    }

    private String buildS3Url(String bucket, String objectKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, s3Region, objectKey);
    }

    private int nextSortOrder(Long attachmentId) {
        return loadAttachmentFiles(attachmentId).stream()
                .map(FileResponseDto::getSortOrder)
                .filter(order -> order != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private AttachmentResponseDto toAttachmentResponse(Attachment attachment) {
        return AttachmentResponseDto.builder()
                .attachmentPublicId(attachment.getPublicId())
                .refType(attachment.getRefType())
                .refPublicId(attachment.getRefPublicId())
                .uploadedByUserPublicId(attachment.getUploadedByUserPublicId())
                .files(loadAttachmentFiles(attachment.getId()))
                .build();
    }

    private List<FileResponseDto> loadAttachmentFiles(Long attachmentId) {
        List<FileResponseDto> mediaFiles = mediaFileRepository.findByAttachmentIdAndStatusOrderBySortOrderAsc(attachmentId, Status.ACTIVE)
                .stream()
                .map(this::toFileResponse)
                .toList();

        List<FileResponseDto> documentFiles = documentFileRepository.findByAttachmentIdAndStatusOrderBySortOrderAsc(attachmentId, Status.ACTIVE)
                .stream()
                .map(this::toFileResponse)
                .toList();

        return Stream.concat(mediaFiles.stream(), documentFiles.stream())
                .sorted((left, right) -> Integer.compare(left.getSortOrder(), right.getSortOrder()))
                .collect(Collectors.toList());
    }

    private FileResponseDto findFileResponse(Attachment attachment, String filePublicId) {
        Optional<MediaFile> mediaFile = mediaFileRepository.findByPublicIdAndAttachmentIdAndStatus(filePublicId, attachment.getId(), Status.ACTIVE);
        if (mediaFile.isPresent()) {
            return toFileResponse(mediaFile.get());
        }

        DocumentFile documentFile = documentFileRepository.findByPublicIdAndAttachmentIdAndStatus(filePublicId, attachment.getId(), Status.ACTIVE)
                .orElseThrow(() -> new FileException(FileErrorCode.FILE_NOT_FOUND));
        return toFileResponse(documentFile);
    }

    private void updateExistingFileSortOrder(Attachment attachment, String filePublicId, Integer sortOrder) {
        if (!StringUtils.hasText(filePublicId) || sortOrder == null) {
            throw new FileException(FileErrorCode.INVALID_FILE_REQUEST);
        }

        Optional<MediaFile> mediaFile = mediaFileRepository.findByPublicIdAndAttachmentIdAndStatus(filePublicId, attachment.getId(), Status.ACTIVE);
        if (mediaFile.isPresent()) {
            mediaFile.get().updateSortOrder(sortOrder);
            return;
        }

        DocumentFile documentFile = documentFileRepository.findByPublicIdAndAttachmentIdAndStatus(filePublicId, attachment.getId(), Status.ACTIVE)
                .orElseThrow(() -> new FileException(FileErrorCode.FILE_NOT_FOUND));
        documentFile.updateSortOrder(sortOrder);
    }

    private FileResponseDto toFileResponse(MediaFile mediaFile) {
        return FileResponseDto.builder()
                .attachmentPublicId(mediaFile.getAttachment().getPublicId())
                .filePublicId(mediaFile.getPublicId())
                .fileType(FileType.MEDIA_FILE)
                .originalFileName(mediaFile.getOriginalFileName())
                .fileName(mediaFile.getFileName())
                .filePath(mediaFile.getFilePath())
                .fileThumbPath(mediaFile.getFileThumbPath())
                .size(mediaFile.getSize())
                .mimeType(mediaFile.getMimeType())
                .sortOrder(mediaFile.getSortOrder())
                .uploadedByUserPublicId(mediaFile.getUploadedByUserPublicId())
                .build();
    }

    private FileResponseDto toFileResponse(DocumentFile documentFile) {
        return FileResponseDto.builder()
                .attachmentPublicId(documentFile.getAttachment().getPublicId())
                .filePublicId(documentFile.getPublicId())
                .fileType(FileType.DOCUMENT_FILE)
                .originalFileName(documentFile.getOriginalFileName())
                .fileName(documentFile.getFileName())
                .filePath(documentFile.getFilePath())
                .fileThumbPath(null)
                .size(documentFile.getSize())
                .mimeType(documentFile.getMimeType())
                .sortOrder(documentFile.getSortOrder())
                .uploadedByUserPublicId(documentFile.getUploadedByUserPublicId())
                .build();
    }

    private Map<Integer, MultipartFile> indexFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Map.of();
        }

        return java.util.stream.IntStream.range(0, files.size())
                .boxed()
                .collect(Collectors.toMap(index -> index, files::get));
    }

    private String defaultFileName(String originalFileName, String storedFileName) {
        return StringUtils.hasText(originalFileName) ? originalFileName : storedFileName;
    }
}
