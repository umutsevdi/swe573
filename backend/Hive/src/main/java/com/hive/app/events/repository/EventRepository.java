package com.hive.app.events.repository;

import com.hive.app.events.model.EventEntity;
import com.hive.app.events.model.EventType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<EventEntity, Long> {
    List<EventEntity> findByOwner_IdAndType(Long id, EventType type);
}
