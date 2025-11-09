package com.hive.config.exceptions;

import jakarta.servlet.ServletException;

public class UserNotFoundException extends ServletException {
    public UserNotFoundException() {
        super("User not found!");
    }
}
