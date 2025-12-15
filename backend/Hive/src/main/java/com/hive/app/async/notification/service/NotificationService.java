package com.hive.app.async.notification.service;

import com.hive.app.account.model.UserEntity;
import com.hive.app.async.notification.model.NotificationEntity;
import com.hive.app.async.notification.model.NotificationType;
import com.hive.app.async.notification.repository.NotificationRepository;
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
    public void insertNotification(UserEntity owner,
            NotificationType type,
            String message,
            String redirectTo) {
        NotificationEntity notification = new NotificationEntity();
        notification.setOwner(owner);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setRedirectTo(redirectTo);
        repository.save(notification);
        log.info("Notification created for user {}: {}", owner.getUsername(), type);
    }

    public List<NotificationEntity> getUserNotifications(UserEntity owner) {
        return repository.findByOwnerOrderByCreatedAtDesc(owner);
    }

    public int markAsRead(Long notificationId, Long userId) {
        return repository.markAsRead(notificationId, userId);
    }

    public int markAllAsRead(Long userId) {
        return repository.markAllAsRead(userId);
    }

    public long getUnreadCount(UserEntity owner) {
        return repository.countByOwnerAndIsReadFalse(owner);
    }
}
