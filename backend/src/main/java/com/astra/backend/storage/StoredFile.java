package com.astra.backend.storage;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stored_files")
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "storage_filename", nullable = false, length = 100)
    private String storageFilename;

    @Column(name = "content_type", nullable = false, length = 150)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(nullable = false, length = 30)
    private String purpose;

    @Column(name = "owner_username", length = 190)
    private String ownerUsername;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "storage_path", nullable = false, length = 1000)
    private String storagePath;

    @Lob
    @Column(name = "file_content", nullable = false,
            columnDefinition = "LONGBLOB")
    private byte[] fileContent;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected StoredFile() {
    }

    public Long getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStorageFilename() { return storageFilename; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public String getPurpose() { return purpose; }
    public String getOwnerUsername() { return ownerUsername; }
    public Long getEntityId() { return entityId; }
    public String getStoragePath() { return storagePath; }
    public byte[] getFileContent() { return fileContent; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setOriginalFilename(String value) {
        this.originalFilename = value;
    }

    public void setStorageFilename(String value) {
        this.storageFilename = value;
    }

    public void setContentType(String value) {
        this.contentType = value;
    }

    public void setFileSize(long value) {
        this.fileSize = value;
    }

    public void setPurpose(String value) {
        this.purpose = value;
    }

    public void setOwnerUsername(String value) {
        this.ownerUsername = value;
    }

    public void setEntityId(Long value) {
        this.entityId = value;
    }

    public void setStoragePath(String value) {
        this.storagePath = value;
    }

    public void setFileContent(byte[] value) {
        this.fileContent = value;
    }
}