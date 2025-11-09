package com.hive.app.events.model;

import com.hive.app.account.model.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Table(name = "EVENT",
        indexes = {
                @Index(name = "EVENT_OWNER_IDX", columnList = "OWNER_ID")
        }
)
@Entity
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 256)
    private String title;
    @Column(length = 1024)
    private String description;
    @Column(length = 512)
    private String tags;
    @Column
    private Double latitude;
    @Column
    private Double longitude;
    @Column
    private String locationName;
    @Column
    private Integer maxParticipants;
    @Column
    @Enumerated(EnumType.ORDINAL)
    private EventType type;
    @Column
    private Integer duration;
    @Column
    private LocalDate eventDate;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @Column
    private LocalDateTime updatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID", nullable = false)
    private UserEntity owner;
}
