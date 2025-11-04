package com.hive.app.image.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "images")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID();
    @Column
    Long userId;
    @Column(nullable = false)
    private String contentType;
    @Column(nullable = false)
    private Long size;
    @Lob
    @Column(nullable = false)
    private byte[] data;
}
