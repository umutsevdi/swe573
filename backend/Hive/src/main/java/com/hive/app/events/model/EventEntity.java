package com.hive.app.events.model;

import com.hive.app.account.model.UserEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Table(name = "EVENT")
@Entity
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    String title;
    @Column
    String description;
    @Column
    String tags;
    @Column
    String backgroundImage;
    @Column
    Double latitude;
    @Column
    Double longitude;
    @Column
    String locationName;
    @Column
    Integer maxParticipants;
    @Column
    @Enumerated(EnumType.ORDINAL)
    EventType type;
    @Column
    Integer duration;
    @Column
    LocalDateTime createdAt;
    @Column
    LocalDateTime updatedAt;
    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    private UserEntity owner;
}
