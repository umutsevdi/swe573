package com.hive.app.async.notification.repository;

import com.hive.app.async.notification.model.NotificationEntity;
import com.hive.app.account.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    
    List<NotificationEntity> findByOwnerOrderByCreatedAtDesc(UserEntity owner);
    
    List<NotificationEntity> findByOwnerAndIsReadFalseOrderByCreatedAtDesc(UserEntity owner);
    
    @Modifying
    @Query("""
    UPDATE NotificationEntity n
    SET n.isRead = true
    WHERE n.id = :notificationId
    AND n.owner.id = :userId
    """)
    int markAsRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
    
    @Modifying
    @Query("""
        UPDATE NotificationEntity n
        SET n.isRead = true
        WHERE n.owner.id = :userId
    """)
    int markAllAsRead(@Param("userId") Long userId);
    
    long countByOwnerAndIsReadFalse(UserEntity owner);
}
