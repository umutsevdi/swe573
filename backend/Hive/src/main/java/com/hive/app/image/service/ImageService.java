package com.hive.app.image.service;

import com.hive.app.events.service.EventService;
import com.hive.app.image.model.ImageEntity;
import com.hive.app.image.repository.ImageRepository;
import com.hive.config.NotFoundException;
import com.hive.config.AuthenticationFailureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository repo;
    private final EventService eventService;

    @Transactional
    public void storeProfile(MultipartFile file,
                             Long id,
                             Long userId
    ) throws HttpMediaTypeNotAcceptableException {
        if (!userId.equals(id)) {
            throw new AuthenticationFailureException();
        }
        try {
            validate(file);
            repo.save(new ImageEntity(
                    ImageEntity.AssetType.PROFILE,
                    userId,
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
                .orElseThrow(AuthenticationFailureException::new);
        try {
            validate(file);
            repo.save(new ImageEntity(
                    ImageEntity.AssetType.EVENT,
                    id,
                    file.getContentType(),
                    file.getSize(),
                    file.getBytes()
            ));
        } catch (IOException e) {
            throw new HttpMediaTypeNotAcceptableException(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ImageEntity loadImage(Long id, ImageEntity.AssetType assetType) throws NotFoundException {
        return repo.findByParentIdAndAssetType(id, assetType)
                .orElseThrow(NotFoundException::new);
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
