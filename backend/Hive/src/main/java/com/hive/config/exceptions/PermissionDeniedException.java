package com.hive.config.exceptions;

import org.springframework.security.access.AccessDeniedException;

public class PermissionDeniedException extends AccessDeniedException {
    public PermissionDeniedException() {
        super("User does not have permission to perform that action.");
    }
}
