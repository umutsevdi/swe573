package com.hive.app.async.transaction.model.request;

import com.hive.app.async.transaction.model.Identifier;
import com.hive.app.async.transaction.model.TransactionType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionRequest {
    List<TransactionType> type;
    Identifier id;
    String message;
    LocalDateTime before;
    LocalDateTime after;
}
