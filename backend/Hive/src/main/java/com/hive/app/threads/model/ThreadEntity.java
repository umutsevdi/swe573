package com.hive.app.threads.model;

import com.hive.app.account.model.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
@Table(name = "THREAD",
        indexes = {
                @Index(name = "THREAD_OWNER_IDX", columnList = "OWNER_ID")
        }
)
@Entity
public class ThreadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String title;
    @Column
    private String tags;
    @Column
    private boolean active;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @Column
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID", nullable = false)
    private UserEntity owner;


    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ThreadMessageEntity> messages = new ArrayList<>();
}
