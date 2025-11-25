package com.hive.app.events.repository;

import com.hive.app.events.model.EventEntity;
import com.hive.app.events.model.EventListView;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends CrudRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {
    Optional<EventEntity> findByCancelledIsFalseAndId(Long id);

    List<EventListView> findByOwner_Id(Long ownerId);

    List<EventListView> findByCancelledIsFalseAndOwner_IdAndEventDateAfter(Long ownerId, LocalDate date);

}
