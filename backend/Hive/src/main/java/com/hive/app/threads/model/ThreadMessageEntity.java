package com.hive.app.threads.model;

import com.hive.app.account.model.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Table(name = "THREAD_MESSAGE",
        indexes = {
                @Index(name = "THREAD_MESSAGE_IDX", columnList = "OWNER_ID"),
                @Index(name = "THREAD_ID_IDX", columnList = "THREAD_ID")
        }
)

@Entity
public class ThreadMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(length = 400)
    private String message;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID", nullable = false)
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "THREAD_ID")
    private ThreadEntity thread;
}
