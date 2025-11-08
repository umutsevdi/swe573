package com.hive.app.threads.service;

import com.hive.app.account.repository.UserRepository;
import com.hive.app.threads.mapper.ThreadMapper;
import com.hive.app.threads.model.*;
import com.hive.app.threads.repository.ThreadMessageRepository;
import com.hive.app.threads.repository.ThreadRepository;
import com.hive.config.NotFoundException;
import com.hive.config.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThreadService {

    private final ThreadRepository threads;
    private final ThreadMessageRepository messages;
    private final ThreadMapper mapper = ThreadMapper.INSTANCE;
    private final UserRepository users;
    private final UserRepository userRepository;

    @Transactional
    public ThreadDTO newForum(ThreadRequest request, Long userId) throws UserNotFoundException {
        ThreadEntity entity = new ThreadEntity();
        entity.setTags(Strings.join(request.getTags(), ','));
        entity.setOwner(userRepository.findById(userId).orElseThrow(UserNotFoundException::new));
        entity.setTitle(request.getTitle());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setActive(true);
        return mapper.entityToDto(threads.save(entity));
    }

    @Transactional
    public ThreadDTO updateThread(ThreadRequest request, Long threadId, Long userId) throws NotFoundException {
        ThreadEntity entity =
                threads.findByIdAndOwnerIdAndActive(threadId, userId, true).orElseThrow(NotFoundException::new);
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
    public ThreadMessageDTO newReply(Long userId, Long threadId, String message) throws NotFoundException {
        ThreadMessageEntity entity = new ThreadMessageEntity();
        entity.setThread(threads.findByIdAndActive(threadId, true).orElseThrow(NotFoundException::new));
        entity.setOwner(users.findByIdAndIsActiveTrue(userId).orElseThrow(NotFoundException::new));
        entity.setMessage(message);
        entity.setCreatedAt(LocalDateTime.now());
        return mapper.entityToDto(messages.save(entity));
    }

    public ThreadDTO getThread(Long threadId) throws NotFoundException {
        return mapper.entityToDto(threads.findById(threadId).map(
                        obj -> {
                            obj.getMessages().sort(Comparator.comparing(ThreadMessageEntity::getCreatedAt));
                            return obj;
                        })
                .orElseThrow(NotFoundException::new));
    }

    public List<ThreadInfo> getThreads(Boolean show) {
        if (show == null) {
            return threads.findAllProjectedBy();
        }
        return threads.findAllByActive(show);
    }

    public void closeThread(Long threadId, Long userId) throws NotFoundException {
        ThreadEntity entity = threads.findByIdAndOwnerIdAndActive(threadId, userId, true).
                orElseThrow(NotFoundException::new);
        entity.setActive(false);
        threads.save(entity);
    }
}
