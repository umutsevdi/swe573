package com.hive.app.image.controller;

import com.hive.app.account.service.UserService;
import com.hive.app.image.model.ImageEntity;
import com.hive.app.image.service.ImageService;
import com.hive.config.NotFoundException;
import com.hive.util.JwtPayload;
import com.hive.util.JwtUtil;
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
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping(path = "/profile/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadProfilePhoto(@RequestPart("file") MultipartFile file,
                                                   @PathVariable Long id
    ) throws HttpMediaTypeNotAcceptableException {
        Long userId = ((JwtPayload) SecurityContextHolder.getContext().getAuthentication().getCredentials()).id();
        service.storeProfile(file, id, userId);
        return ResponseEntity.ok().build();
    }


    @PostMapping(path = "/event/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadEventPhoto(@RequestPart("file") MultipartFile file,
                                                 @PathVariable Long id
    ) throws HttpMediaTypeNotAcceptableException {
        Long userId = ((JwtPayload) SecurityContextHolder.getContext().getAuthentication().getCredentials()).id();
        service.storeEvent(file, id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<Resource> downloadProfile(
            @PathVariable Long id
    ) throws NotFoundException, IllegalArgumentException {
        ImageEntity entity = service.loadImage(id, ImageEntity.AssetType.PROFILE);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entity.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + entity.getParentId() + "\"")
                .body(new ByteArrayResource(entity.getData()));
    }

    @GetMapping("/event/{id}")
    public ResponseEntity<Resource> downloadEvent(@PathVariable Long id) throws NotFoundException,
            IllegalArgumentException {
        ImageEntity entity = service.loadImage(id, ImageEntity.AssetType.EVENT);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(entity.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + entity.getParentId() + "\"")
                .body(new ByteArrayResource(entity.getData()));
    }
}
