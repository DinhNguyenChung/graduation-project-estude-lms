package org.example.estudebackendspring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.estudebackendspring.dto.UserDTO;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.mapper.UserMapper;
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
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    @GetMapping()
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestParam(required = false) String role) {
        try {
            log.info("Getting all users with role filter: {}", role);
            List<User> users;
            
            if (role != null && !role.isEmpty()) {
                users = userRepository.findByRole(org.example.estudebackendspring.enums.UserRole.valueOf(role));
            } else {
                users = userRepository.findAll();
            }
            
            // Convert to DTOs
            List<UserDTO> userDTOs = users.stream()
                    .map(userMapper::toDTO)
                    .collect(Collectors.toList());
            
            log.info("Found {} users", userDTOs.size());
            return ResponseEntity.ok(userDTOs);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid role parameter: {}", role);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{userId}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Convert to DTO
            UserDTO userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
