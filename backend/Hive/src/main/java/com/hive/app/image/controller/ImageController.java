package com.hive.app.image.controller;

import com.hive.app.account.model.UserDTO;
import com.hive.app.image.model.ImageEntity;
import com.hive.app.image.service.ImageService;
import com.hive.config.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/assets/")
public class ImageController {
    private final ImageService service;

    @PostMapping(path = "/{type}/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadProfilePhoto(@RequestPart("file") MultipartFile file,
                                                   @PathVariable ImageEntity.AssetType type,
                                                   @PathVariable Long id
    ) throws HttpMediaTypeNotAcceptableException, NotFoundException {
        Long userId = ((UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        switch (type) {
            case EVENT -> service.storeEvent(file, userId, id);
            case PROFILE, BG -> service.storeUserImage(file, type, userId, id);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<Resource> downloadProfile(
            @PathVariable ImageEntity.AssetType type,
            @PathVariable Long id
    ) throws IllegalArgumentException {
        ImageEntity entity = service.loadImage(id, type);
        if (entity == null) {
            return ResponseEntity.status(HttpStatus.MOVED_TEMPORARILY)
                    .location(URI.create("/assets/" + type.name().toLowerCase())).build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entity.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + entity.getParentId() + "\"")
                .body(new ByteArrayResource(entity.getData()));
    }


    @GetMapping("/{type}")
    public ResponseEntity<Resource> downloadDefault(
            @PathVariable ImageEntity.AssetType type) throws IllegalArgumentException {
        ImageEntity entity = service.loadDefault(type);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entity.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + entity.getParentId() + "\"")
                .body(new ByteArrayResource(entity.getData()));
    }
}
