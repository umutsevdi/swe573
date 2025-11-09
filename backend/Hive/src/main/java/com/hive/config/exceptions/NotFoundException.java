package com.hive.config.exceptions;

import jakarta.servlet.ServletException;

public class NotFoundException extends ServletException {

    public NotFoundException() {
        super("Content not found!");
    }
}
