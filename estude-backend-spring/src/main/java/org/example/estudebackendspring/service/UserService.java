package org.example.estudebackendspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for User operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final S3Service s3Service;
    
    /**
     * Update user avatar
     * Upload new avatar to S3, delete old avatar if exists, update database
     * 
     * @param userId ID of the user
     * @param avatarFile New avatar image file
     * @return Updated user with new avatar path
     */
    @Transactional
    public User updateAvatar(Long userId, MultipartFile avatarFile) {
        log.info("Updating avatar for user ID: {}", userId);
        
        // 1. Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        log.info("Found user: {} ({})", user.getFullName(), user.getEmail());
        
        // 2. Validate file
        if (!s3Service.isValidImageFile(avatarFile)) {
            throw new IllegalArgumentException("Invalid image file. Only JPEG, PNG, GIF, WEBP are allowed.");
        }
        
        // Check file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (avatarFile.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }
        
        log.info("File validation passed: type={}, size={} bytes", 
                avatarFile.getContentType(), avatarFile.getSize());
        
        // 3. Delete old avatar from S3 if exists
        String oldAvatarPath = user.getAvatarPath();
        if (oldAvatarPath != null && !oldAvatarPath.isEmpty()) {
            log.info("Deleting old avatar: {}", oldAvatarPath);
            s3Service.deleteFile(oldAvatarPath);
        }
        
        // 4. Upload new avatar to S3
        String newAvatarUrl = s3Service.uploadFile(avatarFile, "avatars");
        log.info("New avatar uploaded: {}", newAvatarUrl);
        
        // 5. Update user in database
        user.setAvatarPath(newAvatarUrl);
        User savedUser = userRepository.save(user);
        
        log.info("Avatar updated successfully for user ID: {}", userId);
        
        return savedUser;
    }
    
    /**
     * Remove user avatar
     * Delete avatar from S3 and set avatarPath to null
     * 
     * @param userId ID of the user
     * @return Updated user with null avatar path
     */
    @Transactional
    public User removeAvatar(Long userId) {
        log.info("Removing avatar for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        String oldAvatarPath = user.getAvatarPath();
        if (oldAvatarPath != null && !oldAvatarPath.isEmpty()) {
            log.info("Deleting avatar: {}", oldAvatarPath);
            s3Service.deleteFile(oldAvatarPath);
            
            user.setAvatarPath(null);
            userRepository.save(user);
            
            log.info("Avatar removed successfully for user ID: {}", userId);
        } else {
            log.info("User has no avatar to remove");
        }
        
        return user;
    }
}
