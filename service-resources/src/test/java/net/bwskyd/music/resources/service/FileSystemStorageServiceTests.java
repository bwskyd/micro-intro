package net.bwskyd.music.resources.service;

import exception.FileNotFoundException;
import exception.InvalidFileException;
import exception.InvalidFileTypeException;
import exception.StorageException;
import net.bwskyd.music.resources.config.properties.StorageProperties;
import net.bwskyd.music.resources.service.impl.FileSystemStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import util.FSUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileSystemStorageServiceTests {
    private final Path storeLocation = Path.of("/temp");
    List<String> acceptTypes = List.of("audio/mpeg");
    private final String filename = "filename.mp3";
    private final MockMultipartFile file = new MockMultipartFile(
            "filename",
            filename,
            "audio/mpeg",
            "not empty".getBytes(StandardCharsets.UTF_8)
    );
    private FileSystemStorageService storageService;

    @BeforeEach
    public void setup() {
        StorageProperties storageProperties = Mockito.mock(StorageProperties.class);
        when(storageProperties.getLocation()).thenReturn(storeLocation.toString());
        storageService = new FileSystemStorageService(storageProperties);
    }

    @Test
    void givenNullStoreLocation_whenConstruct_thenThrownException() {
        StorageProperties storageProperties = Mockito.mock(StorageProperties.class);
        when(storageProperties.getLocation()).thenReturn(null);

        assertThrows(StorageException.class,
                () -> new FileSystemStorageService(storageProperties));
    }

    @Test
    void givenEmptyStoreLocation_whenConstruct_thenThrownException() {
        StorageProperties storageProperties = Mockito.mock(StorageProperties.class);
        when(storageProperties.getLocation()).thenReturn("  ");

        assertThrows(StorageException.class,
                () -> new FileSystemStorageService(storageProperties));
    }

    @Test
    void givenCorrectStoreLocation_whenConstruct_thenCorrect() {
        StorageProperties storageProperties = Mockito.mock(StorageProperties.class);

        when(storageProperties.getLocation()).thenReturn(storeLocation.toString());
        FileSystemStorageService storageService = new FileSystemStorageService(storageProperties);

        assertEquals(storeLocation.resolve(filename), storageService.resolveStoreLocation(filename));
    }

    @Test
    void givenNullFile_whenStore_thenThrownException() {
        assertThrows(InvalidFileException.class,
                () -> storageService.store(null, acceptTypes));
    }

    @Test
    void givenEmptyFile_whenStore_thenThrownException() {
        MockMultipartFile file = new MockMultipartFile(
                "filename",
                filename,
                "audio/mpeg",
                new byte[0]
        );

        assertThrows(InvalidFileException.class,
                () -> storageService.store(file, acceptTypes));
    }

    @Test
    void givenNullMimeTypeFile_whenStore_thenThrownException() {
        MockMultipartFile file = new MockMultipartFile(
                "filename",
                filename,
                null,
                " ".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(InvalidFileTypeException.class,
                () -> storageService.store(file, acceptTypes));
    }

    @Test
    void givenNotAllowedMimeTypeFile_whenStore_thenThrownException() {
        MockMultipartFile file = new MockMultipartFile(
                "filename",
                filename,
                "application/octet-stream",
                " ".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(InvalidFileException.class,
                () -> storageService.store(file, acceptTypes));
    }

    @Test
    void givenEmptyAcceptTypesList_whenStore_thenThrownException() {
        assertThrows(InvalidFileException.class,
                () -> storageService.store(file, List.of()));
    }

    @Test
    void givenCorrectFile_whenStore_thenThrownSaveException() {
        FileSystemStorageService spyService = Mockito.spy(storageService);

        try (MockedStatic<FSUtil> utilities = Mockito.mockStatic(FSUtil.class)) {
            utilities.when(() -> FSUtil.copyFile(any(InputStream.class), any(Path.class)))
                    .thenThrow(IOException.class);
            utilities.when(() -> FSUtil.generateFilename(eq(file.getOriginalFilename())))
                    .thenReturn(file.getOriginalFilename());

            assertThrows(StorageException.class,
                    () -> spyService.store(file, acceptTypes));

            utilities.verify(() -> FSUtil.copyFile(any(InputStream.class), any(Path.class)), times(1));
            utilities.verify(() -> FSUtil.generateFilename(eq(file.getOriginalFilename())), times(1));
        }
    }

    @Test
    void givenCorrectFile_whenStore_thenReturnOK() {
        FileSystemStorageService spyService = Mockito.spy(storageService);

        try (MockedStatic<FSUtil> utilities = Mockito.mockStatic(FSUtil.class)) {
            utilities.when(() -> FSUtil.copyFile(any(InputStream.class), any(Path.class)))
                    .thenAnswer(i -> null);
            utilities.when(() -> FSUtil.generateFilename(eq(file.getOriginalFilename())))
                    .thenReturn(file.getOriginalFilename());

            assertEquals(file.getOriginalFilename(), spyService.store(file, acceptTypes));

            utilities.verify(() -> FSUtil.copyFile(any(InputStream.class), any(Path.class)), times(1));
            utilities.verify(() -> FSUtil.generateFilename(eq(file.getOriginalFilename())), times(1));
        }
    }

    @Test
    void givenNonExistingFilename_whenDownload_thenThrowFileNotFoundException() {
        String filename = "non-existing-filename";

        FileSystemStorageService spyService = Mockito.spy(storageService);
        doReturn(false).when(spyService).exists(eq(filename));

        assertThrows(FileNotFoundException.class,
                () -> spyService.download(filename));
    }

    @Test
    void givenEmptyFile_whenDownload_thenThrowFileNotFoundException() {
        String filename = "existing-filename";
        File mockFile = Mockito.mock(File.class);
        FileSystemStorageService spyService = Mockito.spy(storageService);

        when(mockFile.isFile()).thenReturn(false);
        doReturn(true).when(spyService).exists(eq(filename));

        try (MockedStatic<FSUtil> utilities = Mockito.mockStatic(FSUtil.class)) {
            utilities.when(() -> FSUtil.getFile(any(Path.class)))
                    .thenReturn(mockFile);

            assertThrows(FileNotFoundException.class,
                    () -> spyService.download(filename));

            utilities.verify(() -> FSUtil.getFile(any(Path.class)), times(1));
        }
    }

    @Test
    void givenEmptyFile_whenDownload_whenReadFileBytes_thenThrowIOException() {
        String filename = "existing-filename";
        FileSystemStorageService spyService = Mockito.spy(storageService);

        doReturn(true).when(spyService).exists(eq(filename));

        try (MockedStatic<FSUtil> utilities = Mockito.mockStatic(FSUtil.class)) {
            utilities.when(() -> FSUtil.getFile(any(Path.class)))
                    .thenThrow(IOException.class);

            assertThrows(StorageException.class,
                    () -> spyService.download(filename));

            utilities.verify(() -> FSUtil.getFile(any(Path.class)), times(1));
        }
    }

    @Test
    void givenEmptyFile_whenDownload_thenDownloadSuccess() {
        String filename = "existing-filename";
        File mockFile = Mockito.mock(File.class);
        FileSystemStorageService spyService = Mockito.spy(storageService);
        byte[] bytes = new byte[0];

        when(mockFile.isFile()).thenReturn(true);
        doReturn(true).when(spyService).exists(eq(filename));

        try (MockedStatic<FSUtil> utilities = Mockito.mockStatic(FSUtil.class)) {
            utilities.when(() -> FSUtil.getFile(any(Path.class)))
                    .thenReturn(mockFile);
            utilities.when(() -> FSUtil.getBytes(eq(mockFile)))
                    .thenReturn(bytes);

            assertEquals(bytes, spyService.download(filename));


            utilities.verify(() -> FSUtil.getFile(any(Path.class)), times(1));
            utilities.verify(() -> FSUtil.getBytes(eq(mockFile)), times(1));
        }
    }

    @Test
    void givenNonExistingFilename_whenDeleteFile_thenThrownException() {
        String filename = "non-existing-filename";
        FileSystemStorageService spyService = Mockito.spy(storageService);

        doReturn(false).when(spyService).exists(eq(filename));

        try (MockedStatic<FSUtil> utilities = Mockito.mockStatic(FSUtil.class)) {
            utilities.when(() -> FSUtil.deleteFile(any(Path.class)))
                    .thenAnswer(i -> null);

            assertThrows(StorageException.class,
                    () -> spyService.delete(filename));

            verify(spyService, times(1)).exists(eq(filename));
            utilities.verify(() -> FSUtil.deleteFile(any(Path.class)), never());
        }
    }

    @Test
    void givenCorrectFilename_whenDeleteFile_thenThrownException() {
        String filename = "existing-filename";
        FileSystemStorageService spyService = Mockito.spy(storageService);

        doReturn(true).when(spyService).exists(eq(filename));

        try (MockedStatic<FSUtil> utilities = Mockito.mockStatic(FSUtil.class)) {
            utilities.when(() -> FSUtil.deleteFile(any(Path.class)))
                    .thenThrow(IOException.class);

            assertThrows(StorageException.class,
                    () -> spyService.delete(filename));

            verify(spyService, times(1)).exists(eq(filename));
            utilities.verify(() -> FSUtil.deleteFile(any(Path.class)), times(1));
        }
    }

    @Test
    void givenCorrectFilename_whenDeleteFile_thenSuccess() {
        String filename = "existing-filename";
        FileSystemStorageService spyService = Mockito.spy(storageService);

        doReturn(true).when(spyService).exists(eq(filename));

        try (MockedStatic<FSUtil> utilities = Mockito.mockStatic(FSUtil.class)) {
            utilities.when(() -> FSUtil.deleteFile(any(Path.class)))
                    .thenAnswer(i -> null);

            spyService.delete(filename);

            verify(spyService, times(1)).exists(eq(filename));
            utilities.verify(() -> FSUtil.deleteFile(any(Path.class)), times(1));
        }
    }
}
