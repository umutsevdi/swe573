package com.hive.config;

import jakarta.servlet.ServletException;

public class NotFoundException extends ServletException {

    public NotFoundException() {
        super("Content not found!");
    }
}
