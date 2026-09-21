package com.astra.media.controller;

import com.astra.dto.MediaUploadResponse;
import com.astra.service.MediaStorageService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaStorageService mediaStorageService;

    public MediaController(
            MediaStorageService mediaStorageService
    ) {

        this.mediaStorageService =
                mediaStorageService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MediaUploadResponse>
    upload(

            @RequestParam("file")
            MultipartFile file

    ) {

        MediaStorageService.StoredFile storedFile =
                mediaStorageService.store(
                        file,
                        "general"
                );

        String url =
                "/uploads/general/"
                        + storedFile.storedFileName();

        MediaUploadResponse response =
                new MediaUploadResponse(
                        storedFile.storedFileName(),
                        storedFile.originalFileName(),
                        storedFile.contentType(),
                        storedFile.fileSize(),
                        url
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}