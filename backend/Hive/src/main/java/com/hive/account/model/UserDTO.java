package com.hive.account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
public class UserDTO implements UserDetails {
    private Long id;
    private String username;
    @JsonIgnore
    private String password; // Bcrypt Hashed
    private String email;
    private String fullName;
    private String profileUrl;
    private String biography;
    private List<String> interests;
    private UserRole role;
    @JsonIgnore
    private boolean isActive;
    @JsonIgnore
    private boolean isAccountTerminated;
    private LocalDateTime createDate;
    private int userScore;
    private int timeBank;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive && !isAccountTerminated;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
