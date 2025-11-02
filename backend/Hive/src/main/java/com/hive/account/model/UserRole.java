package com.hive.account.model;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    MEMBER, ADMIN, OWNER;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
