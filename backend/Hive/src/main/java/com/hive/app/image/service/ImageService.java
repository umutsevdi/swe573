package com.hive.app.image.service;

import com.hive.app.events.service.EventService;
import com.hive.app.image.model.ImageEntity;
import com.hive.app.image.repository.ImageRepository;
import com.hive.config.exceptions.PermissionDeniedException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository repo;
    private final EventService eventService;

    private final Map<ImageEntity.AssetType, ImageEntity> images = new HashMap<>();

    @PostConstruct
    private void init() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (Resource r : resolver.getResources("classpath:/static/**/*.png")) {
            String filename = r.getFilename();
            if (filename == null) {
                continue;
            }
            ImageEntity.AssetType type =
                    ImageEntity.AssetType.valueOf(filename.replaceFirst("(?i)\\.png$", "").toUpperCase());
            images.put(type, new ImageEntity(
                    null,
                    null,
                    type,
                    "images/png",
                    r.contentLength(),
                    r.getContentAsByteArray()));
        }
    }

    @Transactional
    public void storeUserImage(MultipartFile file,
                               ImageEntity.AssetType assetType,
                               Long id,
                               Long userId
    ) throws HttpMediaTypeNotAcceptableException {
        if (!userId.equals(id)) {
            throw new PermissionDeniedException();
        }
        try {
            validate(file);
            repo.save(new ImageEntity(
                    repo.findByParentIdAndAssetType(userId, assetType).map(ImageEntity::getId).orElse(null),
                    userId,
                    assetType,
                    file.getContentType(),
                    file.getSize(),
                    file.getBytes()
            ));
        } catch (IOException e) {
            throw new HttpMediaTypeNotAcceptableException(e.getMessage());
        }
    }

    @Transactional
    public void storeEvent(MultipartFile file,
                           Long id,
                           Long userId
    ) throws HttpMediaTypeNotAcceptableException {
        eventService.findById(id)
                .filter(event -> event.getOwner().getId().equals(userId))
                .orElseThrow(PermissionDeniedException::new);
        try {
            validate(file);
            repo.save(new ImageEntity(
                    repo.findByParentIdAndAssetType(userId, ImageEntity.AssetType.EVENT).map(ImageEntity::getId).orElse(null),
                    id,
                    ImageEntity.AssetType.EVENT,
                    file.getContentType(),
                    file.getSize(),
                    file.getBytes()
            ));
        } catch (IOException e) {
            throw new HttpMediaTypeNotAcceptableException(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ImageEntity loadImage(Long id, ImageEntity.AssetType assetType) {
        return repo.findByParentIdAndAssetType(id, assetType).orElse(null);
    }

    public ImageEntity loadDefault(ImageEntity.AssetType assetType) {
        return images.get(assetType);
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image/* MIME types are allowed.");
        }
    }
}
