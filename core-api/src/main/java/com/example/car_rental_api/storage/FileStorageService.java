package com.example.car_rental_api.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    private final S3Client s3Client;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    @PostConstruct
    public void init(){
        try{
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        }catch(NoSuchBucketException e){
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile multipartFile){
        String fileName = UUID.randomUUID()+"_"+multipartFile.getOriginalFilename();
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(fileName).build();
        try{
            s3Client.putObject(request, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

            return fileName;
        }catch(IOException e){
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    public void deleteFiles(List<String> filesNames){
        if(filesNames == null || filesNames.isEmpty()){
            return;
        }
        List<ObjectIdentifier> filesToDelete = filesNames.stream()
                .map(key -> ObjectIdentifier
                        .builder()
                        .key(key)
                        .build())
                .toList();

        try{
            s3Client.deleteObjects(request ->
                    request
                            .bucket(bucketName)
                            .delete(deleteRequest ->
                                    deleteRequest
                                            .objects(filesToDelete)));
        }catch(S3Exception e){
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    public boolean hasValidExtension(String fileName) {
        if(fileName==null || fileName.isBlank()){
            return false;
        }
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") ||
                lowerCaseName.endsWith(".jpeg") ||
                lowerCaseName.endsWith(".png") ||
                lowerCaseName.endsWith(".webp");
    }

    public String createUrlForFile(String fileName){
        return minioUrl+"/"+bucketName+"/"+fileName;
    }
}
