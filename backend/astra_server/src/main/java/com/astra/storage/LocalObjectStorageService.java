package com.astra.storage;

import com.astra.service.MediaStorageService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Primary
public class LocalObjectStorageService implements ObjectStorageService {
    private final MediaStorageService mediaStorageService;

    public LocalObjectStorageService(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @Override
    public StoredObject put(MultipartFile file, String folder) {
        MediaStorageService.StoredFile stored = mediaStorageService.store(file, folder);
        return new StoredObject(
                stored.storedFileName(),
                stored.originalFileName(),
                stored.contentType(),
                stored.fileSize(),
                stored.path().toString()
        );
    }

    @Override
    public void delete(String folder, String objectName) {
        mediaStorageService.delete(folder, objectName);
    }
}
