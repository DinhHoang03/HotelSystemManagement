package com.humg.HotelSystemManagement.modules.minio_service.services;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // URL dùng để hiển thị ra bên ngoài (localhost)
    @Value("${minio.external-url}")
    private String externalUrl;

    public String uploadFile(MultipartFile file) {
        try {
            // 1. Kiểm tra và tạo bucket nếu chưa có
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                // Set policy public (để frontend xem được ảnh mà không bị lỗi 403)
                setBucketPublic(bucketName);
                log.info("Bucket '{}' created successfully.", bucketName);
            }

            // 2. Tạo tên file unique
            // Ví dụ: 550e8400-e29b..._avatar.jpg
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // 3. Upload
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // 4. Trả về URL công khai
            // Kết quả: http://localhost:9000/hotel-images/uuid_tenfile.jpg
            return externalUrl + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            log.error("MinIO Upload Error: " + e.getMessage());
            throw new RuntimeException("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    // Config Policy JSON để mở quyền đọc (Read-Only) cho tất cả mọi người
    private void setBucketPublic(String bucketName) throws Exception {
        String policy = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": {"AWS": ["*"]},
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """.formatted(bucketName);

        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build()
        );
    }
}