package com.hive.app.image.controller;

import com.hive.app.account.model.UserDTO;
import com.hive.app.image.model.ImageEntity;
import com.hive.app.image.service.ImageService;
import com.hive.config.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            case EVENT -> service.storeEvent(file, id, userId);
            case PROFILE -> service.storeUserImage(file, type, id, userId);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<Resource> downloadProfile(
            @PathVariable ImageEntity.AssetType type,
            @PathVariable Long id
    ) throws IllegalArgumentException, NotFoundException {
        ImageEntity entity = service.loadImage(id, type);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entity.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + entity.getParentId() + "\"")
                .body(new ByteArrayResource(entity.getData()));
    }
}
