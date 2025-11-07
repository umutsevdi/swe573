package com.hive.config;

import jakarta.servlet.ServletException;

public class UserNotFoundException extends ServletException {
    public UserNotFoundException() {
        super("User not found!");
    }
}
