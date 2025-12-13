package com.hive.app.async.transaction.model;

public record Identifier(Long id, IdType type) {
    public enum IdType {
        USER,
        EVENT,
        THREAD
    }
}

