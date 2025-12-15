package com.hive.app.events.repository;

import com.hive.app.events.model.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    Optional<ReviewEntity> findByEvent_IdAndUser_Id(Long eventId, Long userId);
    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);
}