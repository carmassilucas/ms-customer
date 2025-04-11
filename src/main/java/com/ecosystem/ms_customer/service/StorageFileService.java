package com.ecosystem.ms_customer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

@Service
public class StorageFileService {

    private static final Logger log = LoggerFactory.getLogger(StorageFileService.class);
    private final String endpoint;

    private final String bucketName;

    private final S3Client s3Client;

    public StorageFileService(@Value("${aws.localstack.endpoint}") String endpoint,
                              @Value("${aws.localstack.s3.bucket-name}") String bucketName,
                              S3Client s3Client) {
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        this.s3Client = s3Client;
    }

    public String upload(MultipartFile multipartFile) {
        var name = Instant.now().toEpochMilli() + ".jpeg";

        var request = PutObjectRequest.builder().bucket(this.bucketName).key(name).build();

        try {
            this.s3Client.putObject(request, RequestBody.fromBytes(multipartFile.getBytes()));
        } catch (IOException e) {
            log.error("Erro ao enviar arquivo ao bucket: {}", e.getMessage());
            return null;
        }

        log.info("Upload do arquivo feito com sucesso, retornando uri");
        return this.endpoint + "/" + this.bucketName +  "/" + name;
    }

    public void remove(String path) {
        var name = Arrays.stream(path.split("/")).toList().getLast();

        var request = DeleteObjectRequest.builder().bucket(this.bucketName).key(name).build();

        this.s3Client.deleteObject(request);
        log.info("Arquivo removido do bucket com sucesso");
    }
}
