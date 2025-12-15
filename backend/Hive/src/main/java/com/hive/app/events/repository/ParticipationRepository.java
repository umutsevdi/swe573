package com.hive.app.events.repository;

import com.hive.app.events.model.ParticipantView;
import com.hive.app.events.model.ParticipationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends CrudRepository<ParticipationEntity, Long> {

    Optional<ParticipationEntity> findByOwner_IdAndEvent_Id(Long ownerId, Long eventId);

    @Query("""
                    SELECT e.state as state,
                           e.owner.id as ownerId
                    FROM ParticipationEntity e
                    WHERE e.event.id = :eventId
            """)
    List<ParticipantView> findParticipants(Long eventId);

    List<ParticipationEntity> findAllByEvent_Id(Long eventId);
}
