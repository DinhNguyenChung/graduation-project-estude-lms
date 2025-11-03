package org.example.estudebackendspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

/**
 * Service for handling AWS S3 file operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.s3.region}")
    private String region;
    
    /**
     * Upload file to S3 and return the public URL
     * @param file MultipartFile to upload
     * @param folder Folder path in S3 (e.g., "avatars")
     * @return Public URL of uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            String key = folder + "/" + uniqueFilename;
            
            log.info("Uploading file to S3: bucket={}, key={}", bucketName, key);
            
            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            
            // Upload file
            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromBytes(file.getBytes()));
            
            // Generate public URL
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                    bucketName, region, key);
            
            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;
            
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Failed to read file", e);
        } catch (S3Exception e) {
            log.error("S3 error: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to upload file to S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }
    
    /**
     * Delete file from S3
     * @param fileUrl Full S3 URL of the file
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return;
            }
            
            // Extract key from URL
            // URL format: https://bucket-name.s3.region.amazonaws.com/folder/filename.ext
            String key = extractKeyFromUrl(fileUrl);
            
            if (key == null) {
                log.warn("Could not extract key from URL: {}", fileUrl);
                return;
            }
            
            log.info("Deleting file from S3: bucket={}, key={}", bucketName, key);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", key);
            
        } catch (S3Exception e) {
            log.error("S3 error while deleting: {}", e.awsErrorDetails().errorMessage());
            // Don't throw exception, just log - old file deletion failure shouldn't block update
        }
    }
    
    /**
     * Extract S3 key from full URL
     * @param url Full S3 URL
     * @return S3 key (path after bucket name)
     */
    private String extractKeyFromUrl(String url) {
        try {
            // URL format: https://bucket-name.s3.region.amazonaws.com/folder/filename.ext
            if (!url.contains(".amazonaws.com/")) {
                return null;
            }
            
            int keyStartIndex = url.indexOf(".amazonaws.com/") + ".amazonaws.com/".length();
            return url.substring(keyStartIndex);
            
        } catch (Exception e) {
            log.error("Error extracting key from URL: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate file type for avatar upload
     * @param file File to validate
     * @return true if valid image file
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        // Allow common image formats
        return contentType.equals("image/jpeg") 
            || contentType.equals("image/jpg")
            || contentType.equals("image/png") 
            || contentType.equals("image/gif")
            || contentType.equals("image/webp");
    }
}
