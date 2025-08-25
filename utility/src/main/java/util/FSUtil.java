package util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public abstract class FSUtil {
    public static void deleteFile(Path filepath) throws IOException {
        Files.delete(filepath);
    }

    public static File getFile(Path filepath) throws IOException {
        return new File(filepath.toUri());
    }

    public static byte[] getBytes(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    public static void copyFile(InputStream inputStream, Path destinationPath) throws IOException {
        Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static String generateFilename(String originalFilename) {
        return "%s_%s".formatted(UUID.randomUUID(), originalFilename);
    }
}
