package com.hive.app.events.model;

import com.hive.app.account.model.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(length = 256)
    private String title;
    @Column(length = 1024)
    private String description;
    @Column(length = 512)
    private String tags;
    @Column(name = "L_LAT")
    private Double latitude;
    @Column(name = "L_LON")
    private Double longitude;
    @Column(name = "L_NAME")
    private String locationName;
    @Column
    private Integer maxParticipants;
    @Column
    @Enumerated(EnumType.ORDINAL)
    private EventType type;
    @Column
    private Integer duration;
    @Column
    private LocalDateTime eventDate;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private boolean cancelled;
    @Column
    private LocalDateTime updatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID", nullable = false)
    private UserEntity owner;
}
