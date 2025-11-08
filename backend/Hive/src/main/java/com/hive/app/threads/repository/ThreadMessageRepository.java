package com.hive.app.threads.repository;

import com.hive.app.threads.model.ThreadMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreadMessageRepository extends JpaRepository<ThreadMessageEntity, Long> {
}
