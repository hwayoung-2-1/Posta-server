package com.hwayoung.hwayoungserver.storage;

import com.hwayoung.hwayoungserver.common.ApiException;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements FileStorageService {
    private static final String PUBLIC_URL_MODE = "PUBLIC";

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public StoredFile storePortfolioFile(UUID portfolioId, MultipartFile file) {
        String objectName = "portfolios/" + portfolioId + "/" + UUID.randomUUID() + "-" + safeFilename(file);
        try (InputStream inputStream = file.getInputStream()) {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType(file))
                    .build());
            String storageUrl = objectUrl(objectName);
            return new StoredFile(objectName, storageUrl, accessUrl(objectName, storageUrl));
        } catch (Exception exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "FILE_STORAGE_ERROR",
                    "파일 저장에 실패했습니다."
            );
        }
    }

    @Override
    public void upload(String objectKey, byte[] bytes, String contentType) {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(inputStream, bytes.length, -1)
                    .contentType(contentType == null || contentType.isBlank() ? "application/pdf" : contentType)
                    .build());
        } catch (Exception exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "FILE_STORAGE_ERROR",
                    "파일 저장에 실패했습니다."
            );
        }
    }

    @Override
    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception ignored) {
        }
    }

    @Override
    public String presignedGetUrl(String objectKey, int expirySeconds) {
        try {
            return MinioClient.builder()
                    .endpoint(properties.getPublicUrl())
                    .region(properties.getRegion())
                    .credentials(properties.getAccessKey(), properties.getSecretKey())
                    .build()
                    .getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucket())
                            .object(objectKey)
                            .expiry(expirySeconds)
                            .region(properties.getRegion())
                            .build());
        } catch (Exception exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "FILE_STORAGE_ERROR",
                    "파일 조회 URL 발급에 실패했습니다."
            );
        }
    }

    @Override
    public String objectUrl(String objectKey) {
        String encodedObjectName = Arrays.stream(objectKey.split("/"))
                .map(segment -> UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
        return trimTrailingSlash(properties.getPublicUrl()) + "/" + properties.getBucket() + "/" + encodedObjectName;
    }

    @Override
    public String viewUrl(String objectKey, int expirySeconds) {
        if (PUBLIC_URL_MODE.equalsIgnoreCase(properties.getUrlMode()) || properties.isPublicRead()) {
            return objectUrl(objectKey);
        }
        return presignedGetUrl(objectKey, expirySeconds);
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(properties.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
        }
        if (properties.isPublicRead()) {
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(properties.getBucket())
                    .config(publicReadPolicy())
                    .build());
        }
    }

    private String accessUrl(String objectName, String storageUrl) throws Exception {
        if (PUBLIC_URL_MODE.equalsIgnoreCase(properties.getUrlMode())) {
            return storageUrl;
        }

        return MinioClient.builder()
                .endpoint(properties.getPublicUrl())
                .region(properties.getRegion())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build()
                .getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(properties.getBucket())
                        .object(objectName)
                        .expiry(properties.getPresignedExpirySeconds())
                        .region(properties.getRegion())
                        .build());
    }

    private String safeFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return "portfolio.pdf";
        }
        return StringUtils.cleanPath(originalFilename).replaceAll("[\\\\/]", "_");
    }

    private String contentType(MultipartFile file) {
        return file.getContentType() == null ? "application/pdf" : file.getContentType();
    }

    private String trimTrailingSlash(String value) {
        return value == null ? "" : value.replaceAll("/+$", "");
    }

    private String publicReadPolicy() {
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {
                        "AWS": [
                          "*"
                        ]
                      },
                      "Action": [
                        "s3:GetObject"
                      ],
                      "Resource": [
                        "arn:aws:s3:::%s/*"
                      ]
                    }
                  ]
                }
                """.formatted(properties.getBucket());
    }
}
