package net.bwskyd.music.resources.service;

import exception.InvalidFileException;
import exception.MetadataParseException;
import jakarta.persistence.PersistenceException;
import net.bwskyd.music.entity.entity.Resource;
import net.bwskyd.music.resources.feign.SongsFeignClient;
import net.bwskyd.music.resources.repository.ResourceRepository;
import net.bwskyd.music.resources.service.impl.FileSystemStorageService;
import net.bwskyd.music.resources.service.impl.FilesServiceImpl;
import net.rewerk.music.dto.internal.parser.MP3ParseResultDTO;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FilesServiceTests {
    private MockMultipartFile file;
    private Resource resource;
    private Resource resultResource;
    List<String> acceptTypes = List.of("audio/mpeg");

    @Mock
    private FileSystemStorageService storageService;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private MP3MetadataService mp3MetadataService;
    @Mock(strictness = Mock.Strictness.LENIENT)
    private SongsFeignClient songsFeignClient;
    @InjectMocks
    private FilesServiceImpl filesService;

    @BeforeEach
    void setUp() {
        file = new MockMultipartFile(
                "file",
                "name.mp3",
                "audio/mpeg",
                new byte[0]
        );
        resource = new Resource();
        resource.setFilepath(file.getOriginalFilename());
        resultResource = new Resource();
        resultResource.setId(1L);
        resultResource.setFilepath("name.mp3");
        resultResource.setFiletype("audio/mpeg");
    }

    @Test
    void givenEmptyFile_whenSave_thenThrowException() throws IOException {
        when(storageService.store(eq(file), eq(acceptTypes))).thenThrow(InvalidFileException.class);

        assertThrows(InvalidFileException.class,
                () -> filesService.save(file, acceptTypes));
        verify(resourceRepository, never()).save(any(Resource.class));
        verify(storageService, never()).resolveStoreLocation(any(String.class));
        verify(mp3MetadataService, never()).parse(any(Path.class));
        verify(songsFeignClient, never()).createSong(any(SongCreateRequestDTO.class));
    }

    @Test
    void givenInvalidFileMime_whenSave_thenTrowException() throws IOException {
        when(storageService.store(eq(file), eq(acceptTypes))).thenThrow(InvalidFileException.class);

        assertThrows(InvalidFileException.class,
                () -> filesService.save(file, acceptTypes));
        verify(resourceRepository, never()).save(any(Resource.class));
        verify(mp3MetadataService, never()).parse(any(Path.class));
        verify(storageService, never()).resolveStoreLocation(any(String.class));
        verify(songsFeignClient, never()).createSong(any(SongCreateRequestDTO.class));
    }

    @Test
    void givenCorrectFile_whenSave_thenCreateResourceFailed() throws IOException {
        when(resourceRepository.save(eq(resource))).thenThrow(PersistenceException.class);

        assertThrows(PersistenceException.class,
                () -> resourceRepository.save(resource));

        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(mp3MetadataService, never()).parse(any(Path.class));
        verify(storageService, never()).resolveStoreLocation(any(String.class));
        verify(songsFeignClient, never()).createSong(any(SongCreateRequestDTO.class));
    }

    @Test
    void givenCorrectFile_whenParseTags_thenThrowException() throws IOException {
        when(resourceRepository.save(any(Resource.class))).thenReturn(resultResource);
        when(storageService.resolveStoreLocation(any())).thenReturn(Path.of("/"));
        when(mp3MetadataService.parse(any(Path.class))).thenThrow(MetadataParseException.class);

        assertThrows(MetadataParseException.class,
                () -> filesService.save(file, acceptTypes));

        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(mp3MetadataService, times(1)).parse(any(Path.class));
        verify(storageService, times(1)).resolveStoreLocation(any());
        verify(songsFeignClient, never()).createSong(any(SongCreateRequestDTO.class));
    }

    @Test
    void givenCorrectFile_whenSave_thenSaveOK() throws IOException {
        resultResource.setFilepath(file.getOriginalFilename());
        resultResource.setFiletype(file.getContentType());
        MP3ParseResultDTO mp3ParseResultDTO = MP3ParseResultDTO.builder()
                .name("name")
                .album("album")
                .artist("artist")
                .duration("00:00")
                .year(2025)
                .build();
        SongCreateResponseDTO songCreateResponseDTO = new SongCreateResponseDTO();
        songCreateResponseDTO.setId(1L);

        when(resourceRepository.save(any(Resource.class))).thenReturn(resultResource);
        when(storageService.store(eq(file), eq(acceptTypes))).thenReturn(file.getOriginalFilename());
        when(storageService.resolveStoreLocation(eq(file.getOriginalFilename()))).thenReturn(Path.of("/"));
        when(mp3MetadataService.parse(any(Path.class))).thenReturn(mp3ParseResultDTO);
        when(songsFeignClient.createSong(any(SongCreateRequestDTO.class))).thenReturn(songCreateResponseDTO);
        when(filesService.save(file, acceptTypes)).thenReturn(null);

        // TODO

        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(storageService, times(1)).resolveStoreLocation(any(String.class));
        verify(mp3MetadataService, times(1)).parse(any(Path.class));
        verify(storageService, never()).delete(any(String.class));
    }
}
