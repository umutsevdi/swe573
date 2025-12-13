package com.hive.app.async.transaction.service;

import com.hive.app.async.transaction.model.TransactionRecord;
import com.hive.app.async.transaction.mapper.TransactionMapper;
import com.hive.app.async.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final TransactionMapper mapper;

    @Bean(name = "tx-async")
    public Executor txExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("tx-async-");
        executor.initialize();
        return executor;
    }

    @Async
    public void insertTransaction(TransactionRecord record) {
        repository.save(mapper.toEntity(record));
    }
}
