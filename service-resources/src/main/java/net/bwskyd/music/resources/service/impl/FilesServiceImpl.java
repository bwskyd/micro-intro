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
import net.rewerk.music.dto.internal.files.FileDownloadResultDTO;
import net.rewerk.music.dto.internal.parser.MP3ParseResultDTO;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.resource.ResourceCreateResponseDTO;
import net.rewerk.music.dto.response.resource.ResourcesDeleteResponseDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import util.Util;

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
    public ResourceCreateResponseDTO save(MultipartFile file, List<String> acceptTypes) {
        log.info("FilesServiceImpl: save method called");
        String filename = storageService.store(file, acceptTypes);
        log.info("FilesServiceImpl: file stored");
        Resource resource = null;
        SongCreateResponseDTO songCreateResponseDTO = null;
        MP3ParseResultDTO parseResult;
        ResourceCreateResponseDTO result = new ResourceCreateResponseDTO();
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
            parseResult = mp3MetadataService.parse(storageService.resolveStoreLocation(filename));
            log.info("FilesServiceImpl: parsed MP3 tags");
        } catch (Exception e) {
            log.info("FilesServiceImpl: MP3 metadata was not parsed, rollback");
            this.rollbackCreation(filename, resource, songCreateResponseDTO);
            throw new MetadataParseException("Failed to parse file metadata");
        }
        if (parseResult != null) {
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
        } else {
            log.info("FilesServiceImpl: song metadata parse result is null, rollback");
            this.rollbackCreation(filename, resource, songCreateResponseDTO);
            throw new MetadataParseException("Invalid metadata parse result");
        }
        return result;
    }

    @Override
    public FileDownloadResultDTO downloadFileById(@NotNull Long id) {
        if (id <= 0) {
            throw new BadRequestException("Invalid ID: " + id);
        }
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found"));
        byte[] bytes = storageService.download(resource.getFilepath());
        return FileDownloadResultDTO.builder()
                .filename(resource.getFilepath())
                .filetype(resource.getFiletype())
                .bytes(bytes)
                .build();
    }

    @Override
    public ResourcesDeleteResponseDTO deleteAll(@NotNull String idsString) {
        if (idsString.length() > 200) {
            throw new BadParameterException("ID parameter length can not be more than 200 characters");
        }
        List<Long> ids = Util.parseIdsFromString(idsString, ",");
        List<Resource> resourcesList = resourceRepository.findAllById(ids);
        resourcesList.forEach(r -> {
            storageService.delete(r.getFilepath());
            resourceRepository.delete(r);
        });
        return ResourcesDeleteResponseDTO.builder()
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
