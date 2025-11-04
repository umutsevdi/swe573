package com.hive.app.image.service;

import com.hive.app.image.model.ImageEntity;
import com.hive.app.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository repo;

    @Transactional
    public UUID store(MultipartFile file) throws IOException {
        validate(file);
        return repo.save(new ImageEntity(
                UUID.randomUUID(),
                file.getContentType(),
                file.getSize(),
                file.getBytes()
        )).getId();
    }

    @Transactional
    public void update(UUID id, MultipartFile file) throws IOException {
        validate(file);
        repo.save(new ImageEntity(
                id,
                file.getContentType(),
                file.getSize(),
                file.getBytes()
        ));
    }

    @Transactional(readOnly = true)
    public Resource load(UUID id) {
        return repo.findById(id)
                .map(img -> new ByteArrayResource(img.getData()))
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + id));
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
