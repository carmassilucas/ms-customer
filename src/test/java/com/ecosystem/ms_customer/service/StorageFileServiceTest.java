package com.ecosystem.ms_customer.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
        "aws.localstack.endpoint=http://s3.local",
        "aws.localstack.s3.bucket-name=test-bucket"
})
public class StorageFileServiceTest {
    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private StorageFileService service;

    @Value("${aws.localstack.endpoint}")
    private String endpoint;

    @Value("${aws.localstack.s3.bucket-name}")
    private String bucketName;

    @BeforeEach
    void setup() {
        this.service = new StorageFileService(this.endpoint, this.bucketName, this.s3Client);
    }

    @Test
    @DisplayName("Should be possible upload file to aws s3.")
    void should_be_possible_upload_file_to_aws_s3() throws IOException {
        Mockito.when(this.multipartFile.getBytes()).thenReturn(new byte[]{ });

        var url = this.service.upload(this.multipartFile);

        Assertions.assertTrue(url.startsWith(this.endpoint + "/" + this.bucketName + "/"));
        Mockito.verify(this.s3Client, Mockito.times(1)).putObject(Mockito.any(PutObjectRequest.class), Mockito.any(RequestBody.class));
    }

    @Test
    @DisplayName("Should not be possible upload file to aws s3 when exception throws.")
    void should_not_be_possible_upload_file_to_aws_s3_when_exception_throws() throws IOException {
        Mockito.when(this.multipartFile.getBytes()).thenThrow(new IOException());

        String url = this.service.upload(this.multipartFile);

        Assertions.assertNull(url);
    }
}
