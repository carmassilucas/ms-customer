package com.ecosystem.ms_customer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

@Service
public class StorageFileService {

    @Value("${aws.localstack.endpoint}")
    private String endpoint;

    @Value("${aws.localstack.s3.bucket-name}")
    private String bucketName;

    private final S3Client s3Client;

    public StorageFileService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String upload(MultipartFile multipartFile) {
        var name = Instant.now().toEpochMilli() + ".jpeg";

        File file;
        try {
             file = fromMultipartFile(multipartFile);
        } catch (IOException e) {
            return null;
        }

        var request = PutObjectRequest.builder().bucket(bucketName).key(name).build();

        this.s3Client.putObject(request, file.toPath());

        return endpoint + "/" + bucketName +  "/" + name;
    }

    private File fromMultipartFile(MultipartFile multipartFile) throws IOException {
        var file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));

        try (var fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(multipartFile.getBytes());
        }

        return file;
    }
}
