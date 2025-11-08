package com.hive.app.threads.repository;

import com.hive.app.threads.model.ThreadEntity;
import com.hive.app.threads.model.ThreadInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadRepository extends JpaRepository<ThreadEntity, Long> {

    Optional<ThreadEntity> findByIdAndActive(Long id, boolean active);

    Optional<ThreadEntity> findByIdAndOwnerIdAndActive(Long id, Long ownerId, boolean active);

    List<ThreadInfo> findAllProjectedBy();

    List<ThreadInfo> findAllByActive(boolean active);
}
