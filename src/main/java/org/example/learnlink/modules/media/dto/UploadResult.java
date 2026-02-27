package org.example.learnlink.modules.media.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResult {
    private String key;
    private String url;
    private String fileName;
    private String contentType;
    private Long size;
}