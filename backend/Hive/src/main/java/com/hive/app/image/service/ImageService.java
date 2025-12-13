package com.hive.app.image.service;

import com.hive.app.async.transaction.model.Identifier;
import com.hive.app.async.transaction.model.TransactionRecord;
import com.hive.app.async.transaction.model.TransactionType;
import com.hive.app.async.transaction.service.TransactionService;
import com.hive.app.events.model.response.EventResponse;
import com.hive.app.events.service.EventService;
import com.hive.app.image.model.ImageEntity;
import com.hive.app.image.repository.ImageRepository;
import com.hive.config.exceptions.NotFoundException;
import com.hive.config.exceptions.PermissionDeniedException;
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
    private final TransactionService transactionService;

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
            transactionService.insertTransaction(new TransactionRecord(
                    TransactionType.USER_UPDATE,
                    new Identifier(userId, Identifier.IdType.USER),
                    new Identifier(id, Identifier.IdType.USER),
                    "User updated it's profile picture.")
            );
        } catch (IOException e) {
            throw new HttpMediaTypeNotAcceptableException(e.getMessage());
        }
    }

    @Transactional
    public void storeEvent(MultipartFile file,
                           Long id,
                           Long userId
    ) throws HttpMediaTypeNotAcceptableException, NotFoundException {
        EventResponse event = eventService.getEvent(id);
        if (!event.getOwnerId().equals(userId)) {
            throw new PermissionDeniedException();
        }
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

            transactionService.insertTransaction(new TransactionRecord(
                    TransactionType.EVENT_UPDATE,
                    new Identifier(userId, Identifier.IdType.USER),
                    new Identifier(id, Identifier.IdType.EVENT),
                    "User updated offer/need's picture.")
            );
        } catch (IOException e) {
            throw new HttpMediaTypeNotAcceptableException(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ImageEntity loadImage(Long id, ImageEntity.AssetType assetType) throws NotFoundException {
        return repo.findByParentIdAndAssetType(id, assetType).orElseThrow(NotFoundException::new);
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
