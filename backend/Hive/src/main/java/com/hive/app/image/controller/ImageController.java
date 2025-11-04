package com.hive.app.image.controller;

import com.hive.app.account.model.request.UpdateUserRequest;
import com.hive.app.account.service.UserService;
import com.hive.app.image.model.ImageEntity;
import com.hive.app.image.service.ImageService;
import com.hive.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/assets/")
public class ImageController {
    private final ImageService service;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestPart("file") MultipartFile file,
                                         @RequestHeader(value = "Authorization") String authHeader) {
        Long userId = jwtUtil.extractId(jwtUtil.getBearer(authHeader));
        try {
            UUID id = service.store(file);
            userService.updateUser(
                    userId,
                    UpdateUserRequest.profileUrlRequest(id.toString())
            );
            return ResponseEntity.ok(id.toString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to read uploaded file.");
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> update(@RequestPart("file") MultipartFile file,
                                         @RequestHeader(value = "Authorization") String authHeader
    ) {
        try {
            UUID id = service.update(file);
            return ResponseEntity.ok(id.toString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to read uploaded file.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        try {
            Resource resource = service.load(id);
            ImageEntity meta = service.metadata(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(meta.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + meta.getFilename() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    private Long getId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
    }

}
