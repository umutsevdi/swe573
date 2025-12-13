package com.hive.app.events.repository;

import com.hive.app.events.model.EventEntity;
import com.hive.app.events.model.response.EventListResponse;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends CrudRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {
    Optional<EventEntity> findById(Long id);

    @Query("""
                    SELECT e.id as id,
                     e.title as title,
                     e.tags as tagsString,
                     e.maxParticipants as maxParticipants,
                     e.type as type,
                     e.duration as duration,
                     e.eventDate as eventDate,
                     e.createdAt as createdAt,
                     e.updatedAt as updatedAt,
                     e.owner.id as ownerId,
                     e.cancelled as cancelled,
                     e.latitude as latitude,
                     e.longitude as longitude,
                     e.locationName as locationName
                    FROM EventEntity as e
                    WHERE e.owner.id = :ownerId
            """)
    List<EventListResponse> findByOwner_Id(Long ownerId);

    @Query("""
                    SELECT e.id as id,
                     e.title as title,
                     e.tags as tagsString,
                     e.maxParticipants as maxParticipants,
                     e.type as type,
                     e.duration as duration,
                     e.eventDate as eventDate,
                     e.createdAt as createdAt,
                     e.updatedAt as updatedAt,
                     e.owner.id as ownerId,
                     e.cancelled as cancelled,
                     e.latitude as latitude,
                     e.longitude as longitude,
                     e.locationName as locationName
                    FROM EventEntity as e
                    WHERE e.cancelled = false
                    AND e.owner.id = :ownerId
                    AND e.eventDate > :date
            """)
    List<EventListResponse> findByCancelledIsFalseAndOwner_IdAndEventDateAfter(Long ownerId, LocalDate date);

}
