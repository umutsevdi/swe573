package com.hive.app.async.transaction.model;

public record TransactionRecord(
        TransactionType type,
        Identifier source,
        Identifier destination,
        String message
) {
}
