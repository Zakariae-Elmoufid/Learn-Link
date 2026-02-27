package org.example.learnlink.modules.messaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.media.S3StorageService;
import org.example.learnlink.modules.media.dto.UploadResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages/attachments")
@RequiredArgsConstructor
public class DirectChatAttachmentController {

    private final S3StorageService s3StorageService;

    /**
     * Upload attachment for direct message.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment for direct message")
    public ResponseEntity<UploadResult> uploadAttachment(
            @PathVariable Long conversationId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId
    ) {
        String tempMessageId = UUID.randomUUID().toString();
        UploadResult result = s3StorageService.uploadDirectChatAttachment(
                file, conversationId, tempMessageId);
        return ResponseEntity.ok(result);
    }
}