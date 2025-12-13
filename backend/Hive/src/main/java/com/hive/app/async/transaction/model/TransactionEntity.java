package com.hive.app.async.transaction.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Table(name = "TRX")
@Entity
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false)
    private Long srcId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Identifier.IdType srcType;
    @Column(nullable = false)
    private Long destId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Identifier.IdType destType;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @Column(length = 1024)
    private String message;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
