package com.hive.app.account.model;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    MEMBER, ADMIN, OWNER;

    public static final String[] ADMIN_AUTHORITIES = {ADMIN.getAuthority(), OWNER.getAuthority()};

    @Override
    public String getAuthority() {
        return this.name();
    }
}
