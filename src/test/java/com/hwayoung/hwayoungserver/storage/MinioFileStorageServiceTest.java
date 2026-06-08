package com.hwayoung.hwayoungserver.storage;

import io.minio.MinioClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MinioFileStorageServiceTest {

    @Test
    @DisplayName("presigned URL 생성 시 public URL에 접속하지 않고 region 기반으로 서명한다")
    void presignedGetUrlDoesNotConnectToPublicUrlWhenRegionIsConfigured() {
        MinioProperties properties = new MinioProperties();
        properties.setEndpoint("http://minio:9000");
        properties.setPublicUrl("http://localhost:1");
        properties.setAccessKey("minioadmin");
        properties.setSecretKey("minioadmin123");
        properties.setBucket("hwayoung-portfolios-test");
        properties.setRegion("us-east-1");
        MinioFileStorageService storageService = new MinioFileStorageService(mock(MinioClient.class), properties);

        String url = storageService.presignedGetUrl("portfolios/user/portfolio/original.pdf", 600);

        assertThat(url).startsWith("http://localhost:1/hwayoung-portfolios-test/portfolios/user/portfolio/original.pdf?");
        assertThat(url).contains("X-Amz-Credential=minioadmin%2F");
        assertThat(url).contains("%2Fus-east-1%2Fs3%2Faws4_request");
    }

    @Test
    @DisplayName("객체 URL은 서명 쿼리 없이 public URL과 bucket, object key로 생성한다")
    void objectUrlReturnsPublicObjectUrlWithoutSignatureQuery() {
        MinioProperties properties = new MinioProperties();
        properties.setPublicUrl("https://cdn.example.com/");
        properties.setBucket("hwayoung-portfolios-test");
        MinioFileStorageService storageService = new MinioFileStorageService(mock(MinioClient.class), properties);

        String url = storageService.objectUrl("portfolios/user id/portfolio/first-page.png");

        assertThat(url).isEqualTo("https://cdn.example.com/hwayoung-portfolios-test/portfolios/user%20id/portfolio/first-page.png");
    }

    @Test
    @DisplayName("public read 버킷의 보기 URL은 서명 없는 객체 URL이다")
    void viewUrlReturnsObjectUrlWhenBucketIsPublicRead() {
        MinioProperties properties = new MinioProperties();
        properties.setPublicUrl("https://cdn.example.com");
        properties.setBucket("hwayoung-portfolios-test");
        properties.setPublicRead(true);
        MinioFileStorageService storageService = new MinioFileStorageService(mock(MinioClient.class), properties);

        String url = storageService.viewUrl("portfolios/user/portfolio/first-page.png", 600);

        assertThat(url).isEqualTo("https://cdn.example.com/hwayoung-portfolios-test/portfolios/user/portfolio/first-page.png");
    }

    @Test
    @DisplayName("private 버킷의 보기 URL은 presigned URL이다")
    void viewUrlReturnsPresignedUrlWhenBucketIsPrivate() {
        MinioProperties properties = new MinioProperties();
        properties.setEndpoint("http://minio:9000");
        properties.setPublicUrl("http://localhost:1");
        properties.setAccessKey("minioadmin");
        properties.setSecretKey("minioadmin123");
        properties.setBucket("hwayoung-portfolios-test");
        properties.setRegion("us-east-1");
        MinioFileStorageService storageService = new MinioFileStorageService(mock(MinioClient.class), properties);

        String url = storageService.viewUrl("portfolios/user/portfolio/first-page.png", 600);

        assertThat(url).startsWith("http://localhost:1/hwayoung-portfolios-test/portfolios/user/portfolio/first-page.png?");
        assertThat(url).contains("X-Amz-Signature=");
    }
}
