package com.hive.app.image.repository;

import com.hive.app.image.model.ImageEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    Optional<ImageEntity> findByParentIdAndAssetType(Long userId, ImageEntity.AssetType assetType);
}
