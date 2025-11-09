package com.hive.app.threads.service;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.threads.mapper.ThreadMapper;
import com.hive.app.threads.model.*;
import com.hive.app.threads.repository.ThreadMessageRepository;
import com.hive.app.threads.repository.ThreadRepository;
import com.hive.config.exceptions.NotFoundException;
import com.hive.config.exceptions.PermissionDeniedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ThreadService {

    private final ThreadRepository threads;
    private final ThreadMessageRepository messages;
    private final ThreadMapper mapper;
    private final UserMapper userMapper;

    @Transactional
    public ThreadDTO newForum(ThreadRequest request, UserDTO user) {
        ThreadEntity entity = new ThreadEntity();
        entity.setTags(Strings.join(request.getTags(), ','));
        entity.setOwner(userMapper.dtoToEntity(user));
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setActive(true);
        return mapper.entityToDto(threads.save(entity));
    }

    @Transactional
    public ThreadDTO updateThread(UserDTO user, Long threadId, ThreadRequest request) throws NotFoundException {
        log.info("received {}", request);
        ThreadEntity entity = threads.findById(threadId).orElseThrow(NotFoundException::new);
        if (!(entity.isActive() && entity.getOwner().getId().equals(user.getId()))) {
            throw new PermissionDeniedException();
        }
        boolean updated = false;
        if (request.getTags() != null) {
            entity.setTags(Strings.join(request.getTags(), ','));
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
        }
        return mapper.entityToDto(threads.save(entity));
    }


    @Transactional
    public ThreadMessageProjection newReply(UserDTO user, Long threadId, String message) throws NotFoundException {
        ThreadMessageEntity entity = new ThreadMessageEntity();
        ThreadEntity thread = threads.findById(threadId).orElseThrow(NotFoundException::new);
        if (!thread.isActive()) {
            throw new PermissionDeniedException();
        }
        entity.setThread(thread);
        entity.setOwner(userMapper.dtoToEntity(user));
        entity.setMessage(message);
        entity.setCreatedAt(LocalDateTime.now());
        return mapper.entityToDto(messages.save(entity));
    }

    public ThreadDTO getThread(Long threadId) throws NotFoundException {
        return mapper.entityToDto(threads.findById(threadId).orElseThrow(NotFoundException::new));
    }

    public List<ThreadInfo> getThreads(Boolean show) {
        if (show == null) {
            return threads.findAllProjectedBy();
        }
        return threads.findAllByActive(show);
    }

    public void closeThread(Long threadId, Long userId) throws NotFoundException {
        ThreadEntity entity = threads.findById(threadId).orElseThrow(NotFoundException::new);
        if (!(entity.isActive() && entity.getOwner().getId().equals(userId))) {
            throw new PermissionDeniedException();
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setActive(false);
        threads.save(entity);
    }
}
