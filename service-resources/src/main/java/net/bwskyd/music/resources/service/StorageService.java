package net.bwskyd.music.resources.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

public interface StorageService {
    String store(MultipartFile file, List<String> acceptTypes);

    byte[] download(String filename);

    boolean exists(String filename);

    Path resolveStoreLocation(String filename);

    void delete(String filename);
}
