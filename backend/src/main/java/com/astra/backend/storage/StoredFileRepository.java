package com.astra.backend.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository
        extends JpaRepository<StoredFile, Long> {

    List<StoredFile> findAllByOrderByCreatedAtDesc();

    List<StoredFile> findByOwnerUsernameOrderByCreatedAtDesc(
            String ownerUsername);

    List<StoredFile> findByPurposeAndEntityId(
            String purpose, Long entityId);

    Optional<StoredFile> findFirstByPurposeAndOwnerUsernameOrderByCreatedAtDesc(
            String purpose, String ownerUsername);
}