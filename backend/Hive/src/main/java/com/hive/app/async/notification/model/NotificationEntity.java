package com.hive.app.async.notification.model;

import com.hive.app.account.model.UserEntity;
import com.hive.app.async.transaction.model.Identifier;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table(name = "NOTIFICATION")
@Entity
@Data
@RequiredArgsConstructor
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Column(nullable = false)
    private Long ownerId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Column(nullable = false)
    private Long redirectId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Identifier.IdType redirectType;
    
    @Column(nullable = false)
    private boolean isRead = false;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
