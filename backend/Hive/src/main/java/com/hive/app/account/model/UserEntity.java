package com.hive.app.account.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Table(name = "ACCOUNT")
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password; // Bcrypt Hashed
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false)
    private String biography;
    @Column(nullable = false)
    private String interests;
    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private UserRole role;
    @Column(nullable = false)
    private boolean isActive;
    @Column(nullable = false)
    private boolean isAccountTerminated;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private int userScore = 100;
    @Column(nullable = false)
    private int timeBank = 10;

}
