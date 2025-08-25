package net.bwskyd.music.resources.service;

import exception.*;
import jakarta.persistence.EntityNotFoundException;
import net.bwskyd.music.entity.entity.Resource;
import net.bwskyd.music.resources.feign.SongsFeignClient;
import net.bwskyd.music.resources.repository.ResourceRepository;
import net.bwskyd.music.resources.service.impl.FileSystemStorageService;
import net.bwskyd.music.resources.service.impl.FilesServiceImpl;
import net.rewerk.music.dto.internal.files.FileDownloadResultDTO;
import net.rewerk.music.dto.internal.parser.MP3ParseResultDTO;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.resource.ResourceCreateResponseDTO;
import net.rewerk.music.dto.response.resource.ResourcesDeleteResponseDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import org.apache.commons.lang3.RandomStringUtils;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FilesServiceTests {
    private MockMultipartFile file;
    private Resource resultResource;
    private final Path resolvedPath = Path.of("/");
    List<String> acceptTypes = List.of("audio/mpeg");
    MP3ParseResultDTO mp3ParseResultDTO = MP3ParseResultDTO.builder()
            .name("name")
            .album("album")
            .artist("artist")
            .duration("00:00")
            .year(2025)
            .build();

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
        Resource resource = new Resource();
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
        when(storageService.store(eq(file), eq(acceptTypes))).thenReturn(file.getOriginalFilename());
        when(resourceRepository.save(any(Resource.class))).thenThrow(ResourceCreateException.class);

        assertThrows(ResourceCreateException.class,
                () -> filesService.save(file, acceptTypes));

        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(mp3MetadataService, never()).parse(any(Path.class));
        verify(storageService, never()).resolveStoreLocation(any(String.class));
        verify(songsFeignClient, never()).createSong(any(SongCreateRequestDTO.class));
        verify(storageService, times(1)).delete(any());
    }

    @Test
    void givenCorrectFile_whenParseTags_thenThrowException() throws IOException {
        when(storageService.store(eq(file), eq(acceptTypes))).thenReturn(file.getOriginalFilename());
        when(resourceRepository.save(any(Resource.class))).thenReturn(resultResource);
        when(storageService.resolveStoreLocation(eq(file.getOriginalFilename()))).thenReturn(resolvedPath);
        when(mp3MetadataService.parse(eq(resolvedPath))).thenThrow(MetadataParseException.class);

        assertThrows(MetadataParseException.class,
                () -> filesService.save(file, acceptTypes));

        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(mp3MetadataService, times(1)).parse(any(Path.class));
        verify(storageService, times(1)).resolveStoreLocation(any());
        verify(songsFeignClient, never()).createSong(any(SongCreateRequestDTO.class));
        verify(storageService, times(1)).delete(any());
    }

    @Test
    void givenCorrectFile_whenCreateSong_thenThrownSongCreateException() throws IOException {
        when(storageService.store(eq(file), eq(acceptTypes))).thenReturn(file.getOriginalFilename());
        when(resourceRepository.save(any(Resource.class))).thenReturn(resultResource);
        when(storageService.resolveStoreLocation(eq(file.getOriginalFilename()))).thenReturn(resolvedPath);
        when(mp3MetadataService.parse(eq(resolvedPath))).thenReturn(mp3ParseResultDTO);
        when(songsFeignClient.createSong(any(SongCreateRequestDTO.class))).thenThrow(SongCreateException.class);

        assertThrows(SongCreateException.class,
                () -> filesService.save(file, acceptTypes));

        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(mp3MetadataService, times(1)).parse(any(Path.class));
        verify(storageService, times(1)).resolveStoreLocation(any());
        verify(songsFeignClient, times(1)).createSong(any(SongCreateRequestDTO.class));
        verify(storageService, times(1)).delete(any());
    }

    @Test
    void givenCorrectFile_whenCreateSong_thenReturnedInvalidSongIdAndThrownException() throws IOException {
        SongCreateResponseDTO songCreateResponseDTO = new SongCreateResponseDTO();
        songCreateResponseDTO.setId(100L);

        when(storageService.store(eq(file), eq(acceptTypes))).thenReturn(file.getOriginalFilename());
        when(resourceRepository.save(any(Resource.class))).thenReturn(resultResource);
        when(storageService.resolveStoreLocation(eq(file.getOriginalFilename()))).thenReturn(resolvedPath);
        when(mp3MetadataService.parse(eq(resolvedPath))).thenReturn(mp3ParseResultDTO);
        when(songsFeignClient.createSong(any(SongCreateRequestDTO.class))).thenReturn(songCreateResponseDTO);

        assertThrows(SongCreateException.class,
                () -> filesService.save(file, acceptTypes));

        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(mp3MetadataService, times(1)).parse(any(Path.class));
        verify(storageService, times(1)).resolveStoreLocation(any());
        verify(songsFeignClient, times(1)).createSong(any(SongCreateRequestDTO.class));
        verify(storageService, times(1)).delete(any());
    }

    @Test
    void givenCorrectFile_whenParseMetadata_thenReturnedNullObjectAndThrownException() throws IOException {
        when(storageService.store(eq(file), eq(acceptTypes))).thenReturn(file.getOriginalFilename());
        when(resourceRepository.save(any(Resource.class))).thenReturn(resultResource);
        when(storageService.resolveStoreLocation(eq(file.getOriginalFilename()))).thenReturn(resolvedPath);
        when(mp3MetadataService.parse(eq(resolvedPath))).thenReturn(null);

        assertThrows(MetadataParseException.class,
                () -> filesService.save(file, acceptTypes));

        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(mp3MetadataService, times(1)).parse(any(Path.class));
        verify(storageService, times(1)).resolveStoreLocation(any());
        verify(songsFeignClient, never()).createSong(any(SongCreateRequestDTO.class));
        verify(storageService, times(1)).delete(any());
    }

    @Test
    void givenCorrectFile_whenSave_thenSaveOK() throws IOException {
        resultResource.setFilepath(file.getOriginalFilename());
        resultResource.setFiletype(file.getContentType());
        SongCreateResponseDTO songCreateResponseDTO = new SongCreateResponseDTO();
        songCreateResponseDTO.setId(1L);
        ResourceCreateResponseDTO resourceCreateResponseDTO = new ResourceCreateResponseDTO();
        resourceCreateResponseDTO.setId(1L);

        when(resourceRepository.save(any(Resource.class))).thenReturn(resultResource);
        when(storageService.store(eq(file), eq(acceptTypes))).thenReturn(file.getOriginalFilename());
        when(storageService.resolveStoreLocation(eq(file.getOriginalFilename()))).thenReturn(Path.of("/"));
        when(mp3MetadataService.parse(any(Path.class))).thenReturn(mp3ParseResultDTO);
        when(songsFeignClient.createSong(any(SongCreateRequestDTO.class))).thenReturn(songCreateResponseDTO);

        assertEquals(resourceCreateResponseDTO, filesService.save(file, acceptTypes));

        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(storageService, times(1)).resolveStoreLocation(any(String.class));
        verify(mp3MetadataService, times(1)).parse(any(Path.class));
        verify(songsFeignClient, times(1)).createSong(any(SongCreateRequestDTO.class));
        verify(storageService, never()).delete(any(String.class));
    }

    @Test
    void givenNonExistingId_whenDownloadFile_thenThrownException() {
        when(resourceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> filesService.downloadFileById(1L));

        verify(resourceRepository, times(1)).findById(1L);
        verify(storageService, never()).download(anyString());
    }

    @Test
    void givenCorrectId_whenDownloadFile_thenDownloadOK() {
        byte[] bytes = new byte[0];
        FileDownloadResultDTO fileDownloadResultDTO = FileDownloadResultDTO.builder()
                .filename(resultResource.getFilepath())
                .filetype(resultResource.getFiletype())
                .bytes(bytes)
                .build();

        when(resourceRepository.findById(eq(1L))).thenReturn(Optional.of(resultResource));
        when(storageService.download(eq(resultResource.getFilepath()))).thenReturn(bytes);

        assertEquals(fileDownloadResultDTO, filesService.downloadFileById(1L));

        verify(resourceRepository, times(1)).findById(1L);
        verify(storageService, times(1)).download(anyString());
    }

    @Test
    void givenLongIds_whenDeleteFiles_thenThrownException() {
        String longString = RandomStringUtils.insecure().next(201);

        assertThrows(BadParameterException.class,
                () -> filesService.deleteAll(longString));

        verify(resourceRepository, never()).findAllById(anyList());
        verify(storageService, never()).delete(anyString());
        verify(resourceRepository, never()).delete(any(Resource.class));
    }

    @Test
    void givenCorrectIds_whenDeleteFiles_thenDeleteOK() {
        String ids = "1,2,3";
        List<Long> idsList = List.of(1L, 2L, 3L);
        List<Resource> resources = List.of(resultResource, resultResource, resultResource);
        ResourcesDeleteResponseDTO resultDTO = ResourcesDeleteResponseDTO.builder()
                .ids(idsList)
                .build();

        when(resourceRepository.findAllById(eq(idsList))).thenReturn(resources);

        assertEquals(resultDTO, filesService.deleteAll(ids));

        verify(resourceRepository, times(1)).findAllById(eq(idsList));
        verify(storageService, times(3)).delete(anyString());
        verify(resourceRepository, times(3)).delete(resultResource);
    }
}
