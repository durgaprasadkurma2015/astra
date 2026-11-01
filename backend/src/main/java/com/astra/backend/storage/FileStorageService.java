package com.astra.backend.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileStorageService {

    private static final Set<String> IMAGE_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp");

    private static final Set<String> DOCUMENT_EXTENSIONS =
            Set.of("pdf", "doc", "docx", "xls", "xlsx",
                   "csv", "txt", "rtf", "ppt", "pptx");

    private final StoredFileRepository repository;
    private final Path uploadRoot;
    private final long maxFileSize;

    public FileStorageService(
            StoredFileRepository repository,
            @Value("${app.files.upload-dir:./uploads}") String uploadDir,
            @Value("${app.files.max-file-size:10485760}") long maxFileSize) {

        this.repository = repository;
        this.uploadRoot = Paths.get(uploadDir)
                .toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;

        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Cannot initialize Astra upload directory", ex);
        }
    }

    @Transactional
    public StoredFile upload(
            MultipartFile file,
            String purpose,
            String ownerUsername,
            Long entityId) {

        if (file == null || file.isEmpty()) {
            throw badRequest("Select a non-empty file.");
        }

        if (file.getSize() > maxFileSize) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "File exceeds the configured size limit.");
        }

        String normalizedPurpose = purpose == null
                ? "" : purpose.trim().toUpperCase(Locale.ROOT);

        if (!Set.of("PRODUCT_IMAGE", "DOCUMENT", "PROFILE_IMAGE")
                .contains(normalizedPurpose)) {
            throw badRequest("Unsupported file purpose.");
        }

        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw badRequest("Filename is required.");
        }

        // Never use the client filename as a filesystem path.
        String safeOriginal = Paths.get(
                original.replace('\\', '/'))
                .getFileName().toString();

        if (safeOriginal.isBlank() || safeOriginal.length() > 255) {
            throw badRequest("Invalid filename.");
        }

        String extension = getExtension(safeOriginal);

        if ("PRODUCT_IMAGE".equals(normalizedPurpose)
                || "PROFILE_IMAGE".equals(normalizedPurpose)) {
            if (!IMAGE_EXTENSIONS.contains(extension)) {
                throw badRequest(
                        "Images must be JPG, PNG, or WebP.");
            }
        } else if (!DOCUMENT_EXTENSIONS.contains(extension)) {
            throw badRequest("Unsupported document extension.");
        }

        String detectedType = file.getContentType() == null
                ? "" : file.getContentType().toLowerCase(Locale.ROOT);

        validateContentType(extension, detectedType, normalizedPurpose);

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read uploaded file.");
        }

        // Basic signature checks prevent obvious extension spoofing.
        validateSignature(bytes, extension);

        String generatedName = UUID.randomUUID() + "." + extension;
        Path target = uploadRoot.resolve(generatedName).normalize();

        if (!target.startsWith(uploadRoot)) {
            throw badRequest("Invalid storage path.");
        }

        StoredFile stored = new StoredFile();
        stored.setOriginalFilename(safeOriginal);
        stored.setStorageFilename(generatedName);
        stored.setContentType(resolveContentType(extension));
        stored.setFileSize(bytes.length);
        stored.setPurpose(normalizedPurpose);
        stored.setOwnerUsername(ownerUsername);
        stored.setEntityId(entityId);
        stored.setStoragePath(target.toString());
        stored.setFileContent(bytes);

        boolean written = false;

        try {
            Files.write(target, bytes);
            written = true;

            return repository.saveAndFlush(stored);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to store uploaded file.");
        } catch (RuntimeException ex) {
            if (written) {
                try {
                    Files.deleteIfExists(target);
                } catch (IOException cleanupError) {
                    ex.addSuppressed(cleanupError);
                }
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public StoredFile getRequired(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "File not found."));
    }

    @Transactional(readOnly = true)
    public byte[] readContent(StoredFile file) {
        // MySQL is the authoritative source for file bytes.
        return file.getFileContent();
    }

    @Transactional
    public void delete(Long id) {
        StoredFile file = getRequired(id);

        repository.delete(file);
        repository.flush();

        try {
            Path path = Paths.get(file.getStoragePath())
                    .toAbsolutePath().normalize();

            if (path.startsWith(uploadRoot)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ex) {
            // The database deletion has succeeded. Log and reconcile
            // any leftover disk copy operationally.
        }
    }

    private void validateContentType(
            String extension, String type, String purpose) {

        if (type.isBlank()
                || "application/octet-stream".equals(type)) {
            return;
        }

        if (purpose.endsWith("IMAGE")) {
            boolean valid =
                    ("jpg".equals(extension) || "jpeg".equals(extension))
                            && "image/jpeg".equals(type)
                    || "png".equals(extension)
                            && "image/png".equals(type)
                    || "webp".equals(extension)
                            && "image/webp".equals(type);

            if (!valid) {
                throw badRequest("Image content type does not match extension.");
            }
            return;
        }

        Set<String> allowed = switch (extension) {
            case "pdf" -> Set.of("application/pdf");
            case "doc" -> Set.of("application/msword");
            case "docx" -> Set.of(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "xls" -> Set.of("application/vnd.ms-excel");
            case "xlsx" -> Set.of(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "csv" -> Set.of("text/csv", "application/csv", "text/plain");
            case "txt" -> Set.of("text/plain");
            case "rtf" -> Set.of("application/rtf", "text/rtf");
            case "ppt" -> Set.of("application/vnd.ms-powerpoint");
            case "pptx" -> Set.of(
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation");
            default -> Set.of();
        };

        if (!allowed.contains(type)) {
            throw badRequest("Document content type does not match extension.");
        }
    }

    private void validateSignature(byte[] bytes, String extension) {
        boolean valid = switch (extension) {
            case "jpg", "jpeg" ->
                    bytes.length >= 3
                    && (bytes[0] & 0xff) == 0xff
                    && (bytes[1] & 0xff) == 0xd8
                    && (bytes[2] & 0xff) == 0xff;
            case "png" ->
                    bytes.length >= 8
                    && (bytes[0] & 0xff) == 0x89
                    && bytes[1] == 0x50
                    && bytes[2] == 0x4e
                    && bytes[3] == 0x47;
            case "webp" ->
                    bytes.length >= 12
                    && new String(bytes, 0, 4,
                            java.nio.charset.StandardCharsets.US_ASCII)
                            .equals("RIFF")
                    && new String(bytes, 8, 4,
                            java.nio.charset.StandardCharsets.US_ASCII)
                            .equals("WEBP");
            case "pdf" ->
                    bytes.length >= 5
                    && new String(bytes, 0, 5,
                            java.nio.charset.StandardCharsets.US_ASCII)
                            .equals("%PDF-");
            default -> true;
        };

        if (!valid) {
            throw badRequest("File signature does not match its extension.");
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0 || dot == filename.length() - 1) {
            throw badRequest("File extension is required.");
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String resolveContentType(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" ->
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" ->
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "csv" -> "text/csv";
            case "txt" -> "text/plain";
            case "rtf" -> "application/rtf";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" ->
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> "application/octet-stream";
        };
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, message);
    }
}