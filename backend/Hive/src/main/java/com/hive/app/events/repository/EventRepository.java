package com.hive.app.events.repository;

import com.hive.app.events.model.EventEntity;
import com.hive.app.events.model.EventListView;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {

    List<EventListView> findByOwner_Id(Long ownerId);

    List<EventListView> findByOwner_IdAndEventDateAfter(Long ownerId, LocalDate date);

}
