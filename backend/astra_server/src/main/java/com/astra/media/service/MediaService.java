package com.astra.media.service;

import com.astra.service.MediaStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService {
    private final MediaStorageService storageService;

    public MediaService(MediaStorageService storageService) {
        this.storageService = storageService;
    }

    public MediaStorageService.StoredFile store(MultipartFile file, String folder) {
        return storageService.store(file, folder);
    }

    public void delete(String folder, String storedFileName) {
        storageService.delete(folder, storedFileName);
    }
}
