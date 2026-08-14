package com.example.car_rental_api.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {
    @Value("${minio.url}")
    private String minioUrl;
    @Value("${minio.access.key}")
    private String minioAccessKey;
    @Value("${minio.secret.key}")
    private String minioSecretKey;

    @Bean
    public S3Client s3Client(){
        AwsBasicCredentials credentials = AwsBasicCredentials.create(minioAccessKey, minioSecretKey);

        return S3Client.builder().endpointOverride(URI.create(minioUrl)).forcePathStyle(true).credentialsProvider(StaticCredentialsProvider.create(credentials)).region(Region.EU_WEST_1).build();
    }
}
