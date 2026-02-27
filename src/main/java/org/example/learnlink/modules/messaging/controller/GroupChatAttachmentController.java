package org.example.learnlink.modules.messaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.learnlink.modules.media.S3StorageService;
import org.example.learnlink.modules.media.dto.UploadResult;
import org.example.learnlink.modules.messaging.dto.GroupMessageRequest;
import org.example.learnlink.modules.messaging.dto.GroupMessageResponse;
import org.example.learnlink.modules.messaging.entity.MessageType;
import org.example.learnlink.modules.messaging.service.IGroupMessageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/messages/attachments")
@RequiredArgsConstructor
@Tag(name = "Group Chat Attachments")
public class GroupChatAttachmentController {

    private final S3StorageService s3StorageService;
    private final IGroupMessageService groupMessageService;

    /**
     * Upload file and send message with attachment.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment and send group message")
    public ResponseEntity<GroupMessageResponse> uploadAndSendMessage(
            @PathVariable Long groupId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "content", required = false) String content,
            @RequestHeader("X-User-Id") Long userId
    ) {
        String tempMessageId = UUID.randomUUID().toString();

        // Upload to S3
        UploadResult uploadResult = s3StorageService.uploadGroupChatAttachment(
                file, groupId, tempMessageId);

        // Create message with attachment
        GroupMessageRequest request = GroupMessageRequest.builder()
                .content(content != null ? content : file.getOriginalFilename())
                .type(file.getContentType().startsWith("image/") ? MessageType.IMAGE : MessageType.FILE)
                .attachmentUrl(uploadResult.getKey())
                .attachmentName(uploadResult.getFileName())
                .build();

        GroupMessageResponse response = groupMessageService.sendMessage(
                userId, groupId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Upload file only (without sending message).
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachment only")
    public ResponseEntity<UploadResult> uploadOnly(
            @PathVariable Long groupId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId
    ) {

        String tempId = UUID.randomUUID().toString();
        UploadResult result = s3StorageService.uploadGroupChatAttachment(file, groupId, tempId);
        return ResponseEntity.ok(result);
    }
}