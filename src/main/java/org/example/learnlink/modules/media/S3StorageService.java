package org.example.learnlink.modules.media;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3StorageService
{

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public void upload(String path, MultipartFile file) throws IOException {

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(file.getOriginalFilename())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

    }


    public byte[] downloadFile(String key) {
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(awsProperties.getS3().getBucket())
                        .key(key)
                        .build());

        return objectAsBytes.asByteArray();

    }

}
