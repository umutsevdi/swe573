package com.hive.app.image.repository;

import com.hive.app.image.model.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {
}
