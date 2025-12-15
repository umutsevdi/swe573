package com.hive.app.async.notification.service;

import com.hive.app.account.model.UserDTO;
import com.hive.app.async.notification.model.NotificationEntity;
import com.hive.app.async.notification.model.NotificationType;
import com.hive.app.async.notification.model.response.NotificationResponse;
import com.hive.app.async.transaction.model.Identifier;
import com.hive.app.async.notification.repository.NotificationRepository;
import com.hive.app.async.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.List;
import java.util.concurrent.Executor;

@Log4j2
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    @Bean(name = "nt-async")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-async-");
        executor.initialize();
        return executor;
    }

    @Async
    public NotificationResponse insertNotification(UserDTO owner,
            NotificationType type,
            Identifier redirect) {
        NotificationEntity notification = new NotificationEntity();
        notification.setOwnerId(owner.getId());
        notification.setType(type);
        notification.setRead(false);
        notification.setRedirectId(redirect.id());
        notification.setRedirectType(redirect.type());
        return mapper.entityToResponse(repository.save(notification));
    }

    public List<NotificationResponse> getUserNotifications(UserDTO owner) {
        return repository.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
            .stream()
            .map(mapper::entityToResponse)
            .toList();
    }

    public int markAsRead(Long notificationId, Long userId) {
        return repository.markAsRead(notificationId, userId);
    }

    public int markAllAsRead(Long userId) {
        return repository.markAllAsRead(userId);
    }

    public long getUnreadCount(UserDTO owner) {
        return repository.countByOwnerIdAndIsReadFalse(owner.getId());
    }
}
