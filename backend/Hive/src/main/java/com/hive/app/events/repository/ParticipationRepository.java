package com.hive.app.events.repository;

import com.hive.app.events.model.ParticipantView;
import com.hive.app.events.model.ParticipationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends CrudRepository<ParticipationEntity, Long> {

    Optional<ParticipationEntity> findByOwner_IdAndEvent_Id(Long ownerId, Long eventId);

    List<ParticipantView> findByEvent_Id(Long eventId);
}
