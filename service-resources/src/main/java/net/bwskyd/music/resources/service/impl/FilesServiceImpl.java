package net.bwskyd.music.resources.service.impl;

import exception.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bwskyd.music.entity.entity.Resource;
import net.bwskyd.music.resources.feign.SongsFeignClient;
import net.bwskyd.music.resources.repository.ResourceRepository;
import net.bwskyd.music.resources.service.FilesService;
import net.bwskyd.music.resources.service.MP3MetadataService;
import net.rewerk.music.dto.internal.files.DownloadFileResultDTO;
import net.rewerk.music.dto.internal.parser.MP3ParseResultDTO;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.resource.CreateResourceResponseDTO;
import net.rewerk.music.dto.response.resource.DeleteResourcesResponseDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import util.Util;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilesServiceImpl implements FilesService {
    private final FileSystemStorageService storageService;
    private final ResourceRepository resourceRepository;
    private final MP3MetadataService mp3MetadataService;
    private final SongsFeignClient songsFeignClient;

    @Override
    public CreateResourceResponseDTO save(MultipartFile file, List<String> acceptTypes) {
        log.info("FilesServiceImpl: save method called");
        String filename = storageService.store(file, acceptTypes);
        log.info("FilesServiceImpl: file stored");
        Resource resource = null;
        SongCreateResponseDTO songCreateResponseDTO = null;
        CreateResourceResponseDTO result = new CreateResourceResponseDTO();
        try {
            resource = resourceRepository.save(Resource.builder()
                    .filepath(filename)
                    .filetype(file.getContentType())
                    .build());
            result.setId(resource.getId());
            log.info("FilesServiceImpl: resource entity saved with ID {}", resource.getId());
        } catch (Exception e) {
            log.error("FilesServiceImpl: resource entity save error: {}", e.getMessage());
            this.rollbackCreation(filename, resource, songCreateResponseDTO);
            throw new ResourceCreateException("Failed to save resource");
        }
        try {
            MP3ParseResultDTO parseResult = mp3MetadataService.parse(storageService.resolveStoreLocation(filename));
            log.info("FilesServiceImpl: parsed MP3 tags");
            try {
                songCreateResponseDTO = songsFeignClient.createSong(SongCreateRequestDTO.builder()
                        .id(resource.getId())
                        .name(parseResult.getName())
                        .album(parseResult.getAlbum())
                        .artist(parseResult.getArtist())
                        .duration(parseResult.getDuration())
                        .year(parseResult.getYear())
                        .build());
                log.info("FilesServiceImpl: song metadata created on songs microservice. response: {}",
                        songCreateResponseDTO);
            } catch (Exception e) {
                log.info("FilesServiceImpl: song metadata creating failure. There is no response from service");
                this.rollbackCreation(filename, resource, songCreateResponseDTO);
                throw new SongCreateException("Failed to create song metadata");
            }
            if (!songCreateResponseDTO.getId().equals(resource.getId())) {
                log.info("FilesServiceImpl: song metadata creating failure. Invalid response ID: {}",
                        songCreateResponseDTO.getId());
                this.rollbackCreation(filename, resource, songCreateResponseDTO);
                throw new SongCreateException("Invalid song metadata created");
            }
        } catch (IOException e) {
            log.info("FilesServiceImpl: MP3 metadata was not parsed, rollback");
            this.rollbackCreation(filename, resource, songCreateResponseDTO);
            throw new MetadataParseException("Failed to parse file metadata");
        }
        return result;
    }

    @Override
    public DownloadFileResultDTO downloadFileById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found"));
        byte[] bytes = storageService.download(resource.getFilepath());
        return DownloadFileResultDTO.builder()
                .filename(resource.getFilepath())
                .filetype(resource.getFiletype())
                .bytes(bytes)
                .build();
    }

    @Override
    public DeleteResourcesResponseDTO deleteAll(String idsString) {
        if (idsString.length() > 200) {
            throw new BadParameterException("ID parameter length can not be more than 200 characters");
        }
        List<Long> ids = Util.parseIdsFromString(idsString, ",");
        List<Resource> resourcesList = resourceRepository.findAllById(ids);
        resourcesList.forEach(r -> {
            storageService.delete(r.getFilepath());
            resourceRepository.delete(r);
        });
        return DeleteResourcesResponseDTO.builder()
                .ids(ids)
                .build();
    }

    private void rollbackCreation(@NotNull String filename,
                                  Resource resource,
                                  SongCreateResponseDTO songCreateResponseDTO) {
        log.info("FilesServiceImpl: rollbackCreation method called. filename: {}, resource?: {}, songCreationResponseDTO?: {}",
                filename,
                resource != null,
                songCreateResponseDTO != null);
        storageService.delete(filename);
        if (resource != null) {
            resourceRepository.delete(resource);
        }
        if (songCreateResponseDTO != null) {
            songsFeignClient.deleteSongs(String.valueOf(songCreateResponseDTO.getId()));
        }
    }
}
