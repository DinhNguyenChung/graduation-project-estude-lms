package org.example.estudebackendspring.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.estudebackendspring.dto.*;
import org.example.estudebackendspring.entity.Notification;
import org.example.estudebackendspring.entity.NotificationRecipient;
import org.example.estudebackendspring.entity.Teacher;
import org.example.estudebackendspring.entity.User;
import org.example.estudebackendspring.enums.NotificationTargetType;
import org.example.estudebackendspring.enums.UserRole;
import org.example.estudebackendspring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository recipientRepository;
    private final UserRepository userRepository;
    private final ClazzRepository classRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final PlatformTransactionManager txManager;

    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationRecipientRepository recipientRepository,
            UserRepository userRepository,
            ClazzRepository classRepository,
            ClassSubjectRepository classSubjectRepository,
            PlatformTransactionManager txManager
    ) {
        this.notificationRepository = notificationRepository;
        this.recipientRepository = recipientRepository;
        this.userRepository = userRepository;
        this.classRepository = classRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.txManager = txManager;
    }

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest req, User sender) {
        if (req.getTargetType() == NotificationTargetType.SYSTEM) {
            // Chỉ admin hệ thống hoặc giáo vụ (isAdmin = true) mới được phép
            boolean isSystemAllowed = sender.getRole() == UserRole.ADMIN ||
                    (sender instanceof Teacher && ((Teacher) sender).isAdmin());

            if (!isSystemAllowed) {
                throw new AccessDeniedException("Chỉ admin hoặc giáo vụ mới có thể tạo thông báo toàn hệ thống (SYSTEM)");
            }

            req.setTargetId(null);
        } else {
            if (req.getTargetId() == null) {
                throw new IllegalArgumentException("targetId required for " + req.getTargetType());
            }
        }

        // Giáo viên thường chỉ được tạo CLASS hoặc CLASS_SUBJECT
                if (sender.getRole() == UserRole.TEACHER && !(sender instanceof Teacher && ((Teacher) sender).isAdmin())) {
                    if (!(req.getTargetType() == NotificationTargetType.CLASS ||
                            req.getTargetType() == NotificationTargetType.CLASS_SUBJECT)) {
                        throw new AccessDeniedException("Giáo viên chỉ có thể tạo thông báo cho CLASS hoặc CLASS_SUBJECT");
                    }
                }

        // Admin allowed all targets (change as you wish)

        Notification n = new Notification();
        n.setMessage(req.getMessage());
        n.setSentAt(LocalDateTime.now());
        n.setType(req.getType());
        n.setPriority(req.getPriority());
        n.setSender(sender);
        n.setTargetType(req.getTargetType());
        n.setTargetId(req.getTargetId());
        // set helper schoolId if available
        n.setSchoolId(sender.getSchool() != null ? sender.getSchool().getSchoolId() : null);

        notificationRepository.save(n);

        // Resolve recipients
        List<Long> userIds = resolveRecipientUserIds(n);

        // Deduplicate
        Set<Long> unique = new LinkedHashSet<>(userIds);
        List<NotificationRecipient> recipients = new ArrayList<>(unique.size());
        for (Long uid : unique) {
            // skip if sender equals recipient (optional)
            if (Objects.equals(uid, sender.getUserId())) continue;
            NotificationRecipient nr = new NotificationRecipient();
            nr.setNotification(n);
            User u = new User(uid) { }; // placeholder - do not persist; we'll set reference via getOne later OR fetch user
            // Better: fetch user entity (preferred)
            User userEntity = userRepository.findById(uid).orElse(null);
            if (userEntity == null) continue;
            nr.setUser(userEntity);
            nr.setRead(false);
            recipients.add(nr);
        }

        // Batch save recipients
        if (!recipients.isEmpty()) {
            // If list large, consider chunking
            recipientRepository.saveAll(recipients);
        }

        // Build response
        NotificationResponse resp = new NotificationResponse();
        resp.setNotificationId(n.getNotificationId());
        resp.setMessage(n.getMessage());
        resp.setSentAt(n.getSentAt());
        resp.setType(n.getType());
        resp.setPriority(n.getPriority());
        resp.setTargetType(n.getTargetType());
        resp.setTargetId(n.getTargetId());
        resp.setSchoolId(n.getSchoolId());
        resp.setSender(new UserDTO(sender.getUserId(), sender.getFullName(),sender.getEmail(), sender.getRole()));
        resp.setRecipientCount((long) recipients.size());
        return resp;
    }

    private List<Long> resolveRecipientUserIds(Notification n) {
        switch (n.getTargetType()) {
            case SYSTEM:
                return userRepository.findAllUserIds();
            case SCHOOL:
                return userRepository.findUserIdsBySchoolId(n.getTargetId());
            case CLASS:
                return classRepository.findStudentUserIdsByClassId(n.getTargetId());
            case CLASS_SUBJECT:
                return classSubjectRepository.findStudentUserIdsByClassSubjectId(n.getTargetId());
            default:
                return Collections.emptyList();
        }
    }

//    // Get notifications for current user (paged)
//    public Page<NotificationRecipientDto> getNotificationsForUser(Long userId, Pageable pageable) {
//        Page<NotificationRecipient> page = recipientRepository.findByUserIdWithNotification(userId, pageable);
//        return page.map(nr -> {
//            Notification notif = nr.getNotification();
//            UserDTO s = new UserDTO(notif.getSender().getUserId(), notif.getSender().getFullName(),notif.getSender().getEmail(), notif.getSender().getRole());
//            NotificationRecipientDto dto = new NotificationRecipientDto();
//            dto.setNotificationRecipientId(nr.getNotificationRecipientId());
//            dto.setNotificationId(notif.getNotificationId());
//            dto.setMessage(notif.getMessage());
//            dto.setSentAt(notif.getSentAt());
//            dto.setRead(nr.getRead());
//            dto.setSender(s);
//            dto.setType(notif.getType());
//            dto.setPriority(notif.getPriority());
//            dto.setTargetType(notif.getTargetType());
//            dto.setTargetId(notif.getTargetId());
//            return dto;
//        });
//    }
//
//    // Get sent notifications for a sender
//    public Page<NotificationResponse> getSentNotifications(Long senderId, Pageable pageable) {
//        Page<Notification> page = notificationRepository.findBySender_UserIdOrderBySentAtDesc(senderId, pageable);
//        return page.map(n -> {
//            NotificationResponse resp = new NotificationResponse();
//            resp.setNotificationId(n.getNotificationId());
//            resp.setMessage(n.getMessage());
//            resp.setSentAt(n.getSentAt());
//            resp.setType(n.getType());
//            resp.setPriority(n.getPriority());
//            resp.setTargetType(n.getTargetType());
//            resp.setTargetId(n.getTargetId());
//            resp.setSender(new UserDTO(n.getSender().getUserId(), n.getSender().getFullName(),n.getSender().getEmail(), n.getSender().getRole()));
//            resp.setRecipientCount((long)recipientRepository.findByNotification_NotificationId(n.getNotificationId()).size());
//            return resp;
//        });
//    }
// Get notifications for current user - return List
    public List<NotificationRecipientDto> getNotificationsForUser(Long userId) {
        List<NotificationRecipient> list = recipientRepository.findByUserIdWithNotification(userId);
        return list.stream().map(nr -> {
            Notification notif = nr.getNotification();
            UserDTO s = new UserDTO(
                    notif.getSender().getUserId(),
                    notif.getSender().getFullName(),
                    notif.getSender().getEmail(),
                    notif.getSender().getRole()
            );
            NotificationRecipientDto dto = new NotificationRecipientDto();
            dto.setNotificationRecipientId(nr.getNotificationRecipientId());
            dto.setNotificationId(notif.getNotificationId());
            dto.setMessage(notif.getMessage());
            dto.setSentAt(notif.getSentAt());
            dto.setRead(nr.getRead());
            dto.setSender(s);
            dto.setType(notif.getType());
            dto.setPriority(notif.getPriority());
            dto.setTargetType(notif.getTargetType());
            dto.setTargetId(notif.getTargetId());
            return dto;
        }).collect(Collectors.toList());
    }

    // Get sent notifications for a sender - return List
    public List<NotificationResponse> getSentNotifications(Long senderId) {
        List<Notification> list = notificationRepository.findBySender_UserIdOrderBySentAtDesc(senderId);
        return list.stream().map(n -> {
            NotificationResponse resp = new NotificationResponse();
            resp.setNotificationId(n.getNotificationId());
            resp.setMessage(n.getMessage());
            resp.setSentAt(n.getSentAt());
            resp.setType(n.getType());
            resp.setPriority(n.getPriority());
            resp.setTargetType(n.getTargetType());
            resp.setTargetId(n.getTargetId());
            resp.setSender(new UserDTO(
                    n.getSender().getUserId(),
                    n.getSender().getFullName(),
                    n.getSender().getEmail(),
                    n.getSender().getRole()
            ));
            resp.setRecipientCount(
                    (long) recipientRepository.findByNotification_NotificationId(n.getNotificationId()).size()
            );
            return resp;
        }).collect(Collectors.toList());
    }
    @Transactional
    public NotificationResponse updateNotification(Long id, UpdateNotificationRequest req, User currentUser) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (!notif.getSender().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa thông báo này");
        }

        notif.setMessage(req.getMessage());
        notif.setType(req.getType());
        notif.setPriority(req.getPriority());

        notificationRepository.save(notif);

        return mapToResponse(notif, currentUser);
    }



    @Transactional
    public void deleteNotification(Long notificationId, User currentUser) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (!notif.getSender().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("Bạn không có quyền xóa thông báo này");
        }

        // Xóa recipient trước để tránh constraint violation
        recipientRepository.deleteByNotification_NotificationId(notificationId);

        notificationRepository.delete(notif);
    }
    private NotificationResponse mapToResponse(Notification notif, User sender) {
        long recipientCount = recipientRepository
                .countByNotification_NotificationId(notif.getNotificationId());

        return new NotificationResponse(
                notif.getNotificationId(),
                notif.getMessage(),
                notif.getSentAt(),
                notif.getType(),
                notif.getPriority(),
                notif.getTargetType(),
                notif.getTargetId(),
                notif.getSchoolId(),
                new UserDTO(sender.getUserId(), sender.getFullName(), sender.getEmail(), sender.getRole()),
                recipientCount
        );
    }


}
