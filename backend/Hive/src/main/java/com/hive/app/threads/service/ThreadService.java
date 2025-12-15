package com.hive.app.threads.service;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.UserRole;
import com.hive.app.async.notification.model.NotificationType;
import com.hive.app.async.notification.service.NotificationService;
import com.hive.app.async.transaction.model.Identifier;
import com.hive.app.async.transaction.model.TransactionRecord;
import com.hive.app.async.transaction.model.TransactionType;
import com.hive.app.async.transaction.service.TransactionService;
import com.hive.app.threads.mapper.ThreadMapper;
import com.hive.app.threads.model.ThreadDTO;
import com.hive.app.threads.model.ThreadEntity;
import com.hive.app.threads.model.ThreadMessageEntity;
import com.hive.app.threads.model.ThreadRequest;
import com.hive.app.threads.model.response.ThreadMessageResponse;
import com.hive.app.threads.model.response.ThreadResponse;
import com.hive.app.threads.repository.ThreadMessageRepository;
import com.hive.app.threads.repository.ThreadRepository;
import com.hive.config.exceptions.NotFoundException;
import com.hive.config.exceptions.PermissionDeniedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThreadService {

    private final ThreadRepository threads;
    private final ThreadMessageRepository messages;
    private final ThreadMapper mapper;
    private final UserMapper userMapper;
    private final TransactionService transactionService;
    private final NotificationService notificationService;

    @CacheEvict(cacheNames = "threadList", allEntries = true)
    @Transactional
    public ThreadDTO newForum(ThreadRequest request, UserDTO user) {
        ThreadEntity entity = new ThreadEntity();
        entity.setTags(request.getTags().stream()
                .map(e -> e.toLowerCase().replace(' ', '-'))
                .collect(Collectors.joining(",")));
        entity.setOwner(userMapper.dtoToEntity(user));
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setActive(true);
        var result = mapper.entityToDto(threads.save(entity));
        transactionService.insertTransaction(new TransactionRecord(
                        TransactionType.THREAD_CREATE,
                        new Identifier(user.getId(), Identifier.IdType.USER),
                        new Identifier(result.getId(), Identifier.IdType.THREAD),
                        "User created a forum thread titled 'THREAD_NAME'.".replace("THREAD_NAME", result.getTitle())
                )
        );
        return result;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "threadById", key = "#threadId"),
            @CacheEvict(cacheNames = "threadList", allEntries = true)
    })
    @Transactional
    public ThreadDTO updateThread(UserDTO user, Long threadId, ThreadRequest request) throws NotFoundException {
        ThreadEntity entity = threads.findById(threadId).orElseThrow(NotFoundException::new);
        if (!(entity.isActive() && entity.getOwner().getId().equals(user.getId()))) {
            throw new PermissionDeniedException();
        }
        boolean updated = false;
        if (request.getTags() != null) {
            entity.setTags(request.getTags().stream()
                    .map(e -> e.toLowerCase().replace(' ', '-'))
                    .collect(Collectors.joining(",")));
            updated = true;
        }
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
            updated = true;
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
            updated = true;
        }
        if (updated) {
            entity.setUpdatedAt(LocalDateTime.now());
            var result = mapper.entityToDto(threads.save(entity));
            transactionService.insertTransaction(new TransactionRecord(
                            TransactionType.THREAD_UPDATE,
                            new Identifier(user.getId(), Identifier.IdType.USER),
                            new Identifier(result.getId(), Identifier.IdType.THREAD),
                            "User updated the forum thread titled 'THREAD_NAME'.".replace("THREAD_NAME",
                                    result.getTitle())
                    )
            );
            return result;
        }
        return mapper.entityToDto(entity);
    }


    @Caching(evict = {
            @CacheEvict(cacheNames = "threadById", key = "#threadId"),
            @CacheEvict(cacheNames = "threadList", allEntries = true)
    })
    @Transactional
    public ThreadMessageResponse newReply(UserDTO user, Long threadId, String message) throws NotFoundException {
        ThreadMessageEntity entity = new ThreadMessageEntity();
        ThreadEntity thread = threads.findById(threadId).orElseThrow(NotFoundException::new);
        if (!thread.isActive()) {
            throw new PermissionDeniedException();
        }
        entity.setThread(thread);
        entity.setOwner(userMapper.dtoToEntity(user));
        entity.setMessage(message);
        entity.setCreatedAt(LocalDateTime.now());
        var result = mapper.entityToDto(messages.save(entity));
        transactionService.insertTransaction(new TransactionRecord(
                        TransactionType.THREAD_REPLY,
                        new Identifier(user.getId(), Identifier.IdType.USER),
                        new Identifier(threadId, Identifier.IdType.THREAD),
                        "User posted a thread titled 'THREAD_NAME' the message 'MESSAGE'."
                                .replace("THREAD_NAME", thread.getTitle())
                                .replace("MESSAGE", message.substring(0, Math.min(message.length(), 100)))
                )
        );
        notificationService.insertNotification(
                thread.getMessages().stream()
                        .map(e -> e.getOwner().getId())
                        .filter(e -> !e.equals(user.getId()))
                        .collect(Collectors.toSet()),
                NotificationType.THREAD_REPLY,
                new Identifier(thread.getId(), Identifier.IdType.THREAD)
        );

        return result;
    }

    @Cacheable(cacheNames = "threadById", key = "#threadId")
    public ThreadDTO getThread(Long threadId) throws NotFoundException {
        return mapper.entityToDto(threads.findById(threadId).orElseThrow(NotFoundException::new));
    }

    @Cacheable(cacheNames = "threadList")
    public List<ThreadResponse> getThreads() {
        return threads.findAllProjectedBy().stream().map(mapper::toResponse).toList();
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "threadById", key = "#threadId"),
            @CacheEvict(cacheNames = "threadList", allEntries = true)
    })
    public void closeThread(Long threadId, UserDTO self) throws NotFoundException {
        ThreadEntity entity = threads.findById(threadId).orElseThrow(NotFoundException::new);
        if (!(entity.isActive() && entity.getOwner().getId().equals(self.getId())) &&
                self.getRole() != UserRole.ADMIN && self.getRole() != UserRole.OWNER) {
            throw new PermissionDeniedException();
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setActive(false);
        threads.save(entity);
        transactionService.insertTransaction(new TransactionRecord(
                        TransactionType.THREAD_CLOSE,
                        new Identifier(self.getId(), Identifier.IdType.USER),
                        new Identifier(threadId, Identifier.IdType.THREAD),
                        "User closed a thread titled 'THREAD_NAME'.".replace("THREAD_NAME", entity.getTitle())
                )
        );
        notificationService.insertNotification(
                entity.getMessages().stream()
                        .map(e -> e.getOwner().getId())
                        .filter(e -> !e.equals(self.getId()))
                        .collect(Collectors.toSet()),
                NotificationType.THREAD_CLOSED,
                new Identifier(entity.getId(), Identifier.IdType.THREAD)
        );
    }
}
