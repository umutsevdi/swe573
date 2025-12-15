package com.hive.app.events.model;

import com.hive.app.account.model.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Table(name = "REVIEW",
        indexes = {
                @Index(name = "REVIEW_EVENT_IDX", columnList = "EVENT_ID"),
                @Index(name = "REVIEW_USER_IDX", columnList = "USER_ID")
        }
)
@Entity
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private EventEntity event;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UserEntity user;
    
    @Column(nullable = false)
    private Integer rating;
    
    @Column(name = "DELIVERY_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;
    
    @Column(length = 1024)
    private String message;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}