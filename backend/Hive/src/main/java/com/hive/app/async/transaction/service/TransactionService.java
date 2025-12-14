package com.hive.app.async.transaction.service;

import com.hive.app.async.transaction.mapper.TransactionMapper;
import com.hive.app.async.transaction.model.TransactionEntity;
import com.hive.app.async.transaction.model.TransactionRecord;
import com.hive.app.async.transaction.model.request.TransactionRequest;
import com.hive.app.async.transaction.repository.TransactionRepository;
import com.hive.app.async.transaction.repository.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;

@Log4j2
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

    public List<TransactionRecord> getAllEventsBy(TransactionRequest request) {
        log.info(request);
        Specification<TransactionEntity> spec = Specification.allOf(
                TransactionSpecification.hasIdentifier(request.getId()),
                TransactionSpecification.hasType(request.getType()),
                TransactionSpecification.isAfter(request.getAfter()),
                TransactionSpecification.isBefore(request.getBefore()),
                TransactionSpecification.messageContains(request.getMessage())
        );
        log.info(repository.findAll(spec).stream().map(mapper::toRecord).toList());

        return repository.findAll(spec).stream().map(mapper::toRecord).toList();
    }
}
