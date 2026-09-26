package com.astra.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {
    StoredObject put(MultipartFile file, String folder);
    void delete(String folder, String objectName);

    record StoredObject(String objectName, String originalName, String contentType, long size, String location) {}
}
