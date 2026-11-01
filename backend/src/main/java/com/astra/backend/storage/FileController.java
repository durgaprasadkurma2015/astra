package com.astra.backend.storage;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

	
    private final FileStorageService storage;
    private final StoredFileRepository repository;

    public FileController(
            FileStorageService storage,
            StoredFileRepository repository) {
        this.storage = storage;
        this.repository = repository;
    }
    @io.swagger.v3.oas.annotations.Operation(
    	    summary = "Upload a file",
    	    description = "Admin-only upload for product images and documents."
    	)
    // ADMIN: upload product images or documents.
    @PostMapping("/admin/upload")
    public ResponseEntity<StoredFileResponse> adminUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam String purpose,
            @RequestParam(required = false) Long entityId,
            Authentication auth) {

        if ("PRODUCT_IMAGE".equalsIgnoreCase(purpose)) {
            if (entityId == null || entityId <= 0) {
                throw badRequest(
                        "A valid product ID is required for product images.");
            }
        } else if (entityId != null) {
            throw badRequest(
                    "entityId is only supported for product images.");
        }

        StoredFile saved = storage.upload(
                file, purpose, auth.getName(), entityId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StoredFileResponse.from(saved));
    }

    // USER: upload or replace their own profile picture.
    @PostMapping("/me/profile-picture")
    public ResponseEntity<StoredFileResponse> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        StoredFile saved = storage.upload(
                file, "PROFILE_IMAGE", auth.getName(), null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StoredFileResponse.from(saved));
    }

    // USER: upload a private document.
    @PostMapping("/me/documents")
    public ResponseEntity<StoredFileResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        StoredFile saved = storage.upload(
                file, "DOCUMENT", auth.getName(), null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StoredFileResponse.from(saved));
    }

    // ADMIN: list all stored files.
    @GetMapping("/admin")
    public List<StoredFileResponse> listAllFiles() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(StoredFileResponse::from)
                .toList();
    }

    // USER: list their own documents and profile pictures.
    @GetMapping("/me")
    public List<StoredFileResponse> listMyFiles(Authentication auth) {
        return repository.findByOwnerUsernameOrderByCreatedAtDesc(
                        auth.getName())
                .stream()
                .map(StoredFileResponse::from)
                .toList();
    }

    // ADMIN: delete any stored file.
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> adminDelete(@PathVariable Long id) {
        storage.delete(id);
        return ResponseEntity.noContent().build();
    }

    // USER: delete their own file only.
    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> deleteMyFile(
            @PathVariable Long id,
            Authentication auth) {

        StoredFile file = storage.getRequired(id);

        if (!auth.getName().equals(file.getOwnerUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only delete your own files.");
        }

        storage.delete(id);
        return ResponseEntity.noContent().build();
    }

    // PUBLIC: display product images.
    @GetMapping("/public/{id}/content")
    public ResponseEntity<ByteArrayResource> viewProductImage(
            @PathVariable Long id) {

        StoredFile file = storage.getRequired(id);
        requireProductImage(file);

        return fileResponse(file, false);
    }

    // PUBLIC: download product images.
    @GetMapping("/public/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadProductImage(
            @PathVariable Long id) {

        StoredFile file = storage.getRequired(id);
        requireProductImage(file);

        return fileResponse(file, true);
    }

    // PRIVATE: download a document or profile picture.
    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @PathVariable Long id,
            Authentication auth) {

        StoredFile file = storage.getRequired(id);

        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean owner = auth.getName().equals(file.getOwnerUsername());

        if (!admin && !owner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to download this file.");
        }

        return fileResponse(file, true);
    }

    private ResponseEntity<ByteArrayResource> fileResponse(
            StoredFile file, boolean attachment) {

        String disposition = attachment ? "attachment" : "inline";

        ContentDisposition contentDisposition =
                ContentDisposition.builder(disposition)
                        .filename(file.getOriginalFilename(),
                                StandardCharsets.UTF_8)
                        .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        file.getContentType()))
                .contentLength(file.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(new ByteArrayResource(
                        storage.readContent(file)));
    }

    private void requireProductImage(StoredFile file) {
        if (!"PRODUCT_IMAGE".equals(file.getPurpose())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "File not found.");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, message);
    }
}