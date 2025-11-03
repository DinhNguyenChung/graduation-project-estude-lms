package org.example.estudebackendspring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.repository.UserRepository;
import org.example.estudebackendspring.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    
    @GetMapping()
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    
    /**
     * Update user avatar
     * Upload image to S3 and update avatarPath in database
     * 
     * @param userId User ID
     * @param avatar Image file (JPEG, PNG, GIF, WEBP, max 5MB)
     * @return Updated user with new avatar URL
     */
    @PatchMapping(value = "/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAvatar(
            @PathVariable Long userId,
            @RequestParam("avatar") MultipartFile avatar) {
        try {
            log.info("Received avatar update request for user ID: {}", userId);
            log.info("File info: name={}, size={}, type={}", 
                    avatar.getOriginalFilename(), 
                    avatar.getSize(), 
                    avatar.getContentType());
            
            User updatedUser = userService.updateAvatar(userId, avatar);
            
            // Return response with avatar URL
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avatar updated successfully");
            response.put("userId", updatedUser.getUserId());
            response.put("fullName", updatedUser.getFullName());
            response.put("avatarUrl", updatedUser.getAvatarPath());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
            
        } catch (RuntimeException e) {
            log.error("Error updating avatar: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Remove user avatar
     * Delete avatar from S3 and set avatarPath to null
     * 
     * @param userId User ID
     * @return Success message
     */
    @DeleteMapping("/{userId}/avatar")
    public ResponseEntity<?> removeAvatar(@PathVariable Long userId) {
        try {
            log.info("Received avatar removal request for user ID: {}", userId);
            
            User updatedUser = userService.removeAvatar(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avatar removed successfully");
            response.put("userId", updatedUser.getUserId());
            response.put("fullName", updatedUser.getFullName());
            response.put("avatarUrl", null);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Error removing avatar: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to remove avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
