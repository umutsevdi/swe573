package com.hive.app.image.controller;

import com.hive.app.account.service.UserService;
import com.hive.app.image.model.ImageEntity;
import com.hive.app.image.service.ImageService;
import com.hive.config.AssetNotFoundException;
import com.hive.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/assets/")
public class ImageController {
    private final ImageService service;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping(path = "/{type}/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(@RequestPart("file") MultipartFile file,
                                       @RequestHeader(value = "Authorization") String authHeader,
                                       @PathVariable String type,
                                       @PathVariable Long id
    ) throws HttpMediaTypeNotAcceptableException {
        Long userId = jwtUtil.extractId(jwtUtil.getBearer(authHeader));
        switch (ImageEntity.AssetType.valueOf(type.toUpperCase())) {
            case EVENT -> service.storeEvent(file, id, userId);
            case PROFILE -> service.storeProfile(file, id, userId);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<Resource> download(
            @PathVariable String type,
            @PathVariable Long id
    ) {
        try {
            ImageEntity entity = service.loadImage(id, ImageEntity.AssetType.valueOf(type.toUpperCase()));
            Resource resource = new ByteArrayResource(entity.getData());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(entity.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + entity.getParentId() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException | AssetNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
