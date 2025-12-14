package com.hive.app.async.transaction.model;

import java.time.LocalDateTime;

public record TransactionRecord(
        TransactionType type,
        Identifier source,
        Identifier destination,
        String message,
        LocalDateTime createdAt
) {

    public TransactionRecord(
            TransactionType type,
            Identifier source,
            Identifier destination,
            String message
    ) {
        this(type, source, destination, message, LocalDateTime.now());
    }
}
