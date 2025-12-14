package com.hive.app.threads.repository;

import com.hive.app.threads.model.ThreadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadRepository extends CrudRepository<ThreadEntity, Long> {

    Optional<ThreadEntity> findByIdAndActive(Long id, boolean active);

    Optional<ThreadEntity> findByIdAndOwnerIdAndActive(Long id, Long ownerId, boolean active);

    @Query("""
               SELECT e.id AS id,
                   e.title AS title,
                   e.description AS description,
                   e.createdAt AS createdAt,
                   e.owner.id AS ownerId,
                   e.active AS active,
                   e.tags AS tagsList
                 FROM ThreadEntity e
            """)
    List<ThreadView> findAllProjectedBy();
}
