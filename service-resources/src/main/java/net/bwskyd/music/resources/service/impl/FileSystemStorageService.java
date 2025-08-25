package net.bwskyd.music.resources.service.impl;

import exception.FileNotFoundException;
import exception.InvalidFileException;
import exception.InvalidFileTypeException;
import exception.StorageException;
import jakarta.validation.constraints.NotNull;
import net.bwskyd.music.resources.config.properties.StorageProperties;
import net.bwskyd.music.resources.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import util.FSUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static util.FSUtil.deleteFile;

@Service
public class FileSystemStorageService implements StorageService {
    private final Path storeLocation;

    public FileSystemStorageService(@NotNull StorageProperties storageProperties) {
        if (storageProperties.getLocation() == null || storageProperties.getLocation().trim().isEmpty()) {
            throw new StorageException("File upload location can not be empty");
        }
        this.storeLocation = Paths.get(storageProperties.getLocation());
    }

    @Override
    public String store(MultipartFile file, List<String> acceptTypes) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploading file empty");
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new InvalidFileTypeException("Invalid content type");
        }
        acceptTypes.stream()
                .filter(contentType::equals)
                .findAny()
                .orElseThrow(() -> new InvalidFileException("Invalid content type"));
        String filename = FSUtil.generateFilename(file.getOriginalFilename());
        Path destinationPath = this.storeLocation.resolve(Paths.get(filename))
                .normalize()
                .toAbsolutePath();
        try (InputStream inputStream = file.getInputStream()) {
            FSUtil.copyFile(inputStream, destinationPath);
        } catch (IOException e) {
            throw new StorageException("Failed to store file");
        }
        return filename;
    }



    @Override
    public byte[] download(String filename) {
        if (!this.exists(filename)) {
            throw new FileNotFoundException("File not found");
        }
        File file;
        try {
            file = FSUtil.getFile(storeLocation.resolve(filename));
        } catch (IOException e) {
            throw new StorageException("Failed to download file");
        }
        if (!file.isFile()) {
            throw new FileNotFoundException("Invalid file found");
        }
        try {
            return FSUtil.getBytes(file);
        } catch (IOException e) {
            throw new FileNotFoundException("Failed to download file");
        }
    }

    @Override
    public boolean exists(String filepath) {
        return Files.exists(this.storeLocation.resolve(filepath));
    }

    @Override
    public Path resolveStoreLocation(String filename) {
        return this.storeLocation.resolve(filename);
    }

    @Override
    public void delete(String filepath) {
        if (!this.exists(filepath)) {
            throw new StorageException("File not exists");
        }
        try {
            deleteFile(storeLocation.resolve(filepath));
        } catch (IOException e) {
            throw new StorageException("Failed to delete file");
        }
    }
}
