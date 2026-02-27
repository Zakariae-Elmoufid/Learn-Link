package org.example.learnlink.modules.media;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learnlink.modules.media.dto.UploadResult;
import org.example.learnlink.modules.media.exception.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsProperties awsProperties;

    // Folder constants matching your bucket structure
    public static final String FOLDER_PROFILE = "profile";
    public static final String FOLDER_POST = "post";
    public static final String FOLDER_ATTACHMENT_GROUP = "attachment-group";
    public static final String FOLDER_ATTACHMENT = "attachment";

    // Allowed file types
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip",
            "text/plain"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB


    /**
     * Generic upload - used for profile pictures.
     */
    public String upload(String folder, MultipartFile file) throws IOException {
        String key = folder + "/" + sanitizeFilename(file.getOriginalFilename());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        log.info("File uploaded: {}", key);

        // Return presigned URL for private bucket
        return generatePresignedUrl(key);
    }

    /**
     * Download file as bytes.
     */
    public byte[] downloadFile(String key) {
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(awsProperties.getS3().getBucket())
                        .key(key)
                        .build());
        return objectAsBytes.asByteArray();
    }

    // ========== GROUP CHAT ATTACHMENTS ==========

    /**
     * Upload attachment for group chat message.
     * Stored in: attachment-group/{groupId}/{messageId}_{filename}
     */
    public UploadResult uploadGroupChatAttachment(MultipartFile file, Long groupId, String messageId) {
        validateFile(file);
        String key = String.format("%s/%d/%s_%s",
                FOLDER_ATTACHMENT_GROUP, groupId, messageId, sanitizeFilename(file.getOriginalFilename()));
        return uploadFileWithResult(file, key);
    }

    // ========== DIRECT (1-TO-1) CHAT ATTACHMENTS ==========

    /**
     * Upload attachment for direct message.
     * Stored in: attachment/{conversationId}/{messageId}_{filename}
     */
    public UploadResult uploadDirectChatAttachment(MultipartFile file, Long conversationId, String messageId) {
        validateFile(file);
        String key = String.format("%s/%d/%s_%s",
                FOLDER_ATTACHMENT, conversationId, messageId, sanitizeFilename(file.getOriginalFilename()));
        return uploadFileWithResult(file, key);
    }

    // ========== PROFILE PICTURES ==========

    /**
     * Upload profile picture.
     * Stored in: profile/{userId}_{filename}
     */
    public UploadResult uploadProfilePicture(MultipartFile file, Long userId) {
        String key = String.format("%s/%d_%s",
                FOLDER_PROFILE, userId, sanitizeFilename(file.getOriginalFilename()));
        return uploadFileWithResult(file, key);
    }

    /**
     * Upload profile picture from raw bytes (used for async processing).
     * Stored in: profile/{userId}_{filename}
     */
    public UploadResult uploadProfilePictureFromBytes(byte[] data, String fileName, String contentType, Long userId) {
        String key = String.format("%s/%d_%s",
                FOLDER_PROFILE, userId, sanitizeFilename(fileName));
        return uploadBytesWithResult(data, fileName, contentType, key);
    }

    // ========== POST ATTACHMENTS ==========

    /**
     * Upload post attachment.
     * Stored in: post/{postId}_{filename}
     */
    public UploadResult uploadPostAttachment(MultipartFile file, Long postId) {
        validateFile(file);
        String key = String.format("%s/%d_%s",
                FOLDER_POST, postId, sanitizeFilename(file.getOriginalFilename()));
        return uploadFileWithResult(file, key);
    }

    // ========== CORE UPLOAD METHOD ==========

    private UploadResult uploadFileWithResult(MultipartFile file, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            log.info("File uploaded: {}", key);

            // For private bucket, generate presigned URL
            String url = generatePresignedUrl(key);

            return UploadResult.builder()
                    .key(key)
                    .url(url)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload file: {}", key, e);
            throw new FileUploadException("Failed to upload file", e);
        }
    }

    /**
     * Upload raw bytes to S3 (used for async processing where MultipartFile is unavailable).
     */
    private UploadResult uploadBytesWithResult(byte[] data, String fileName, String contentType, String key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(key)
                .contentType(contentType)
                .contentLength((long) data.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        log.info("File uploaded: {}", key);

        // For private bucket, generate presigned URL
        String url = generatePresignedUrl(key);

        return UploadResult.builder()
                .key(key)
                .url(url)
                .fileName(fileName)
                .contentType(contentType)
                .size((long) data.length)
                .build();
    }

    // ========== PRESIGNED URL GENERATION (FOR PRIVATE BUCKET) ==========

    /**
     * Generate presigned URL valid for 1 hour.
     * This allows clients to access private files temporarily.
     */
    public String generatePresignedUrl(String key) {
        return generatePresignedUrl(key, Duration.ofHours(1));
    }

    /**
     * Generate presigned URL with custom duration.
     */
    public String generatePresignedUrl(String key, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /**
     * Refresh presigned URL for an existing file.
     * Use this when returning messages (URLs may have expired).
     */
    public String refreshUrl(String key) {
        return generatePresignedUrl(key, Duration.ofHours(1));
    }

    // ========== DELETE OPERATIONS ==========

    public void deleteFile(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
            log.info("File deleted: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", key, e);
        }
    }

    public void deleteGroupAttachments(Long groupId) {
        deleteByPrefix(FOLDER_ATTACHMENT_GROUP + "/" + groupId + "/");
    }

    public void deleteConversationAttachments(Long conversationId) {
        deleteByPrefix(FOLDER_ATTACHMENT + "/" + conversationId + "/");
    }

    private void deleteByPrefix(String prefix) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            for (S3Object s3Object : listResponse.contents()) {
                deleteFile(s3Object.key());
            }
            log.info("Deleted all files with prefix: {}", prefix);
        } catch (Exception e) {
            log.error("Failed to delete files with prefix: {}", prefix, e);
        }
    }

    // ========== VALIDATION ==========

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds 10MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileUploadException("File type not allowed: " + contentType);
        }
    }

    private String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return UUID.randomUUID().toString();
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}