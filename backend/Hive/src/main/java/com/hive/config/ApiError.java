package com.hive.config;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String path,
        String message,
        List<String> details) {
}
