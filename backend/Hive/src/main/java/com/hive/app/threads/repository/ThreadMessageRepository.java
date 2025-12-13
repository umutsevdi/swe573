package com.hive.app.threads.repository;

import com.hive.app.threads.model.ThreadMessageEntity;
import com.hive.app.threads.model.response.ThreadMessageResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadMessageRepository extends JpaRepository<ThreadMessageEntity, Long> {
    @Query("""
               SELECT e.message AS message,
                      e.createdAt AS createdAt,
                      e.owner.id AS ownerId
                 FROM ThreadMessageEntity e
                WHERE e.thread.id = :threadId
            """)
    List<ThreadMessageResponse> findAllByThread(long threadId);

}
