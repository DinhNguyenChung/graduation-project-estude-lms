package org.example.estudebackendspring.controller;

import jakarta.validation.Valid;
import org.example.estudebackendspring.dto.CreateNotificationRequest;
import org.example.estudebackendspring.dto.NotificationRecipientDto;
import org.example.estudebackendspring.dto.NotificationResponse;
import org.example.estudebackendspring.dto.UpdateNotificationRequest;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.UserRole;
import org.example.estudebackendspring.service.AuthService;
import org.example.estudebackendspring.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;


    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;

    }

    // Create notification (Admin or Teacher)
    @PostMapping
    public ResponseEntity<?> createNotification(@Valid @RequestBody CreateNotificationRequest req) {
        User sender = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (sender == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // Only ADMIN or TEACHER allowed to create in this example
        if (!(sender.getRole() == UserRole.ADMIN || sender.getRole() == UserRole.TEACHER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Chỉ có quản trị viên hoặc giáo viên mới có thể tạo thông báo");
        }

        try {
            NotificationResponse resp = notificationService.createNotification(req, sender);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (AccessDeniedException ade) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ade.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }

    // Get notifications for current user (student / teacher / admin) - paged
//    @GetMapping("/me")
//    public ResponseEntity<?> getMyNotifications(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        Pageable pageable = PageRequest.of(page, size);
//        Page<NotificationRecipientDto> pageDto = notificationService.getNotificationsForUser(currentUser.getUserId(), pageable);
//        return ResponseEntity.ok(pageDto);
//    }
    @GetMapping("/me")
    public ResponseEntity<?> getMyNotifications() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<NotificationRecipientDto> listDto = notificationService.getNotificationsForUser(currentUser.getUserId());
        return ResponseEntity.ok(listDto);
    }

    // Get notifications sent by current user
//    @GetMapping("/sent")
//    public ResponseEntity<?> getSentNotifications(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        Pageable pageable = PageRequest.of(page, size);
//        Page<NotificationResponse> pageDto = notificationService.getSentNotifications(me.getUserId(), pageable);
//        return ResponseEntity.ok(pageDto);
//    }
    @GetMapping("/sent")
    public ResponseEntity<?> getSentNotifications() {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<NotificationResponse> listDto = notificationService.getSentNotifications(me.getUserId());
        return ResponseEntity.ok(listDto);
    }
    @PutMapping("/{notificationId}")
    public ResponseEntity<?> updateNotification(
            @PathVariable Long notificationId,
            @Valid @RequestBody UpdateNotificationRequest request
    ) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            NotificationResponse resp = notificationService.updateNotification(notificationId, request, currentUser);
            return ResponseEntity.ok(resp);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }


    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            notificationService.deleteNotification(notificationId, currentUser);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
