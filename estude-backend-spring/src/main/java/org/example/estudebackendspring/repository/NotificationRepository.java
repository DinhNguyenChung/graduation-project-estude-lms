package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.Notification;
import org.example.estudebackendspring.enums.NotificationTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface NotificationRepository extends JpaRepository<Notification, Long> {

//    Page<Notification> findBySender_UserIdOrderBySentAtDesc(Long senderId, Pageable pageable);
    // Lấy danh sách thông báo do sender gửi (không cần Pageable nữa)
    @Query("SELECT n FROM Notification n " +
            "JOIN FETCH n.sender s " +
            "LEFT JOIN FETCH s.school " +
            "WHERE s.userId = :senderId " +
            "ORDER BY n.sentAt DESC")
    List<Notification> findBySender_UserIdOrderBySentAtDesc(@Param("senderId") Long senderId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.targetType = :targetType AND n.targetId = :targetId")
    long countByTarget(@Param("targetType") NotificationTargetType targetType, @Param("targetId") Long targetId);
}
