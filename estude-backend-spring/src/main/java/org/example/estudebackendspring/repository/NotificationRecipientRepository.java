package org.example.estudebackendspring.repository;

import org.example.estudebackendspring.entity.NotificationRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {
    // get recipients for a user (with notification loaded lazily — can use fetch join in custom query)
//    @Query("SELECT nr FROM NotificationRecipient nr JOIN FETCH nr.notification n JOIN FETCH n.sender s WHERE nr.user.userId = :userId ORDER BY n.sentAt DESC")
//    Page<NotificationRecipient> findByUserIdWithNotification(@Param("userId") Long userId, Pageable pageable);
    @Query("SELECT nr FROM NotificationRecipient nr " +
            "JOIN FETCH nr.notification n " +
            "JOIN FETCH n.sender s " +
            "LEFT JOIN FETCH s.school " +
            "WHERE nr.user.userId = :userId " +
            "ORDER BY n.sentAt DESC")
    List<NotificationRecipient> findByUserIdWithNotification(@Param("userId") Long userId);

    List<NotificationRecipient> findByNotification_NotificationId(Long notificationId);
    void deleteByNotification_NotificationId(Long notificationId);


    long countByNotification_NotificationId(Long notificationNotificationId);
}
