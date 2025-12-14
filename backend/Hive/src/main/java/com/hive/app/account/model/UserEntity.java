package com.hive.app.account.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Table(name = "ACCOUNT")
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password; // Bcrypt Hashed
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false, length = 1024)
    private String biography;
    @Column(nullable = false, length = 512)
    private String interests;
    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private UserRole role;
    @Column(nullable = false)
    private boolean active;
    @Column(nullable = false)
    private boolean suspended;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private int userScore = 100;
    @Column(nullable = false)
    private int timeBank = 10;
    @Column(nullable = false)
    private int frozenAmount = 0;

}
