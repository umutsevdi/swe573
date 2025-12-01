package com.hive.config.exceptions;

import jakarta.servlet.ServletException;

public class LogicException extends ServletException {
    public LogicException(String message) {
        super(message);
    }
}
