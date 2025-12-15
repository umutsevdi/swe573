package com.hive.app.async.notification.repository;

import com.hive.app.async.notification.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @Modifying
    @Query("""
            UPDATE NotificationEntity n
            SET n.isRead = true
            WHERE n.id = :notificationId
            AND n.ownerId = :userId
            """)
    int markAsRead(Long notificationId, Long userId);

    @Modifying
    @Query("""
                UPDATE NotificationEntity n
                SET n.isRead = true
                WHERE n.ownerId = :userId
            """)
    int markAllAsRead(Long userId);

    long countByOwnerIdAndIsReadFalse(Long ownerId);
}
