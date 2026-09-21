package com.astra.service;

import com.astra.exception.FileStorageException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final long DEFAULT_MAX_FILE_SIZE =
            10 * 1024 * 1024L;

    private final Path uploadRoot;

    private final long maxFileSize;

    public MediaStorageService(
            @Value("${astra.media.upload-dir:uploads}") String uploadDir,
            @Value("${astra.media.max-file-size:10485760}") long maxFileSize
    ) {

        this.uploadRoot = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        this.maxFileSize =
                maxFileSize > 0
                        ? maxFileSize
                        : DEFAULT_MAX_FILE_SIZE;

        try {

            Files.createDirectories(uploadRoot);

        } catch (IOException ex) {

            throw new FileStorageException(
                    "Could not create upload directory: "
                            + uploadRoot,
                    ex
            );
        }
    }

    public StoredFile store(
            MultipartFile file,
            String folder
    ) {

        validateFile(file);

        String originalFileName =
                file.getOriginalFilename();

        String extension =
                getExtension(originalFileName);

        String storedFileName =
                UUID.randomUUID()
                        + extension;

        Path folderPath =
                uploadRoot
                        .resolve(folder)
                        .normalize();

        if (!folderPath.startsWith(uploadRoot)) {
            throw new FileStorageException(
                    "Invalid storage folder"
            );
        }

        try {

            Files.createDirectories(folderPath);

            Path target =
                    folderPath
                            .resolve(storedFileName)
                            .normalize();

            if (!target.startsWith(folderPath)) {
                throw new FileStorageException(
                        "Invalid file path"
                );
            }

            try (InputStream inputStream =
                         file.getInputStream()) {

                Files.copy(
                        inputStream,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return new StoredFile(
                    originalFileName,
                    storedFileName,
                    file.getContentType(),
                    file.getSize(),
                    target
            );

        } catch (IOException ex) {

            throw new FileStorageException(
                    "Could not store file",
                    ex
            );
        }
    }

    public void delete(
            String folder,
            String storedFileName
    ) {

        if (storedFileName == null ||
                storedFileName.isBlank()) {
            return;
        }

        Path folderPath =
                uploadRoot
                        .resolve(folder)
                        .normalize();

        Path filePath =
                folderPath
                        .resolve(storedFileName)
                        .normalize();

        if (!filePath.startsWith(folderPath)) {
            throw new FileStorageException(
                    "Invalid file path"
            );
        }

        try {

            Files.deleteIfExists(filePath);

        } catch (IOException ex) {

            throw new FileStorageException(
                    "Could not delete file: "
                            + storedFileName,
                    ex
            );
        }
    }

    private void validateFile(
            MultipartFile file
    ) {

        if (file == null ||
                file.isEmpty()) {

            throw new FileStorageException(
                    "File must not be empty"
            );
        }

        if (file.getSize() > maxFileSize) {

            throw new FileStorageException(
                    "File size exceeds maximum allowed size of "
                            + maxFileSize
                            + " bytes"
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES
                        .contains(contentType.toLowerCase())) {

            throw new FileStorageException(
                    "Unsupported image type. "
                            + "Allowed types: JPEG, PNG, WEBP, GIF"
            );
        }
    }

    private String getExtension(
            String originalFileName
    ) {

        if (originalFileName == null ||
                originalFileName.isBlank()) {

            return "";
        }

        int lastDot =
                originalFileName.lastIndexOf('.');

        if (lastDot < 0) {
            return "";
        }

        return originalFileName
                .substring(lastDot)
                .toLowerCase();
    }

    public record StoredFile(

            String originalFileName,

            String storedFileName,

            String contentType,

            long fileSize,

            Path path

    ) {
    }
}