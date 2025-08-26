package net.bwskyd.music.resourceservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bwskyd.music.resourceservice.dto.internal.parser.MP3ParseResultDTO;
import net.bwskyd.music.resourceservice.dto.request.song.SongCreateRequestDTO;
import net.bwskyd.music.resourceservice.dto.response.resource.ResourceCreateResponseDTO;
import net.bwskyd.music.resourceservice.dto.response.resource.ResourcesDeleteResponseDTO;
import net.bwskyd.music.resourceservice.dto.response.song.SongCreateResponseDTO;
import net.bwskyd.music.resourceservice.entity.Resource;
import net.bwskyd.music.resourceservice.exception.*;
import net.bwskyd.music.resourceservice.feign.SongsFeignClient;
import net.bwskyd.music.resourceservice.repository.ResourceRepository;
import net.bwskyd.music.resourceservice.service.MP3MetadataService;
import net.bwskyd.music.resourceservice.service.ResourceService;
import net.bwskyd.music.resourceservice.util.CSVUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository resourceRepository;
    private final SongsFeignClient songsFeignClient;
    private final MP3MetadataService mp3MetadataService;

    @Override
    public ResourceCreateResponseDTO save(byte[] bytes, String contentType) {
        final String ALLOWED_CONTENT_TYPE = "audio/mpeg";
        if (!ALLOWED_CONTENT_TYPE.equals(contentType)) {
            throw new BadRequestException("Invalid file format: %s. Only MP3 files are allowed"
                    .formatted(contentType)
            );
        }
        if (bytes.length == 0) {
            throw new InvalidFileException("File is empty");
        }
        log.info("save method called");
        Resource resource = Resource.builder()
                .bytes(bytes)
                .build();
        SongCreateResponseDTO songCreateResponseDTO = null;
        MP3ParseResultDTO parseResult;
        ResourceCreateResponseDTO result = new ResourceCreateResponseDTO();
        try {
            resourceRepository.save(resource);
            result.setId(resource.getId());
            log.info("resource entity saved with ID {}", resource.getId());
        } catch (Exception e) {
            log.error("resource entity save error: {}", e.getMessage());
            this.rollbackCreation(resource, songCreateResponseDTO);
            throw new ResourceCreateException("Failed to save resource");
        }
        try {
            parseResult = mp3MetadataService.parse(bytes);
            log.info("parsed MP3 tags: {}",  parseResult);
        } catch (Exception e) {
            log.info("MP3 metadata was not parsed, rollback");
            this.rollbackCreation(resource, songCreateResponseDTO);
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
                log.info("song metadata created on songs microservice. response: {}",
                        songCreateResponseDTO);
            } catch (Exception e) {
                log.info("song metadata creating failure. There is no response from service");
                this.rollbackCreation(resource, songCreateResponseDTO);
                throw new SongCreateException("Failed to create song metadata");
            }
            if (!songCreateResponseDTO.getId().equals(resource.getId())) {
                log.info("song metadata creating failure. Invalid response ID: {}",
                        songCreateResponseDTO.getId());
                this.rollbackCreation(resource, songCreateResponseDTO);
                throw new SongCreateException("Invalid song metadata created");
            }
        } else {
            log.info("song metadata parse result is null, rollback");
            this.rollbackCreation(resource, songCreateResponseDTO);
            throw new MetadataParseException("Invalid metadata parse result");
        }
        return result;
    }

    @Override
    public byte[] downloadById(String id) {
        if (id.matches(".*\\p{L}.*|.*\\d+[.,]\\d+.*")) {
            throw new BadRequestException("Invalid value '%s' for ID. Must be a positive integer".formatted(id));
        }
        long identifier = Long.parseLong(id.trim());
        if (identifier <= 0) {
            throw new BadRequestException("Invalid value '%d' for ID. Must be a positive integer".formatted(identifier));
        }
        Resource resource = resourceRepository.findById(identifier)
                .orElseThrow(() -> new EntityNotFoundException("Resource with ID=%d not found".formatted(identifier)));
        return resource.getBytes();
    }

    @Override
    public ResourcesDeleteResponseDTO deleteByIds(String ids) {
        final int MAX_IDS_LENGTH = 200;
        if (ids.length() >= MAX_IDS_LENGTH) {
            throw new BadParameterException("CSV string is too long: received %d characters, maximum allowed is 200"
                    .formatted(ids.length()));
        }
        List<Long> idsList = CSVUtil.parseIDs(ids, ',');
        List<Resource> resources = resourceRepository.findAllById(idsList);
        List<Long> idsToDelete = resources.stream().map(Resource::getId).toList();
        if (!idsToDelete.isEmpty()) {
            resourceRepository.deleteAllById(idsList);
            songsFeignClient.deleteSongs(idsToDelete.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","))
            );
        }
        return ResourcesDeleteResponseDTO.builder()
                .ids(idsToDelete)
                .build();
    }

    private void rollbackCreation(Resource resource,
                                  SongCreateResponseDTO songCreateResponseDTO) {
        log.info("rollbackCreation method called, resource?: {}, songCreationResponseDTO?: {}",
                resource != null,
                songCreateResponseDTO != null);
        if (resource != null) {
            resourceRepository.delete(resource);
        }
        if (songCreateResponseDTO != null) {
            songsFeignClient.deleteSongs(String.valueOf(songCreateResponseDTO.getId()));
        }
    }
}
