package net.bwskyd.music.resources.service.impl;

import exception.BadParameterException;
import exception.MetadataParseException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.bwskyd.music.entity.entity.Resource;
import net.bwskyd.music.resources.repository.ResourceRepository;
import net.bwskyd.music.resources.service.FilesService;
import net.bwskyd.music.resources.service.MP3MetadataService;
import net.rewerk.music.dto.internal.files.DownloadFileResultDTO;
import net.rewerk.music.dto.internal.parser.MP3ParseResultDTO;
import net.rewerk.music.dto.response.resource.CreateResourceResponseDTO;
import net.rewerk.music.dto.response.resource.DeleteResourcesResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import util.Util;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilesServiceImpl implements FilesService {
    private final FileSystemStorageService storageService;
    private final ResourceRepository resourceRepository;
    private final MP3MetadataService mp3MetadataService;

    @Override
    public CreateResourceResponseDTO save(MultipartFile file, List<String> acceptTypes) {
        String filename = storageService.store(file, acceptTypes);
        CreateResourceResponseDTO result = new CreateResourceResponseDTO();
        try {
            Resource resource = resourceRepository.save(Resource.builder()
                    .filepath(filename)
                    .filetype(file.getContentType())
                    .build());
            result.setId(resource.getId());
        } catch (Exception e) {
            storageService.delete(filename);
        }
        try {
            MP3ParseResultDTO parseResult = mp3MetadataService.parse(storageService.resolveStoreLocation(filename));
            System.out.println("Parse result is" + parseResult);
        } catch (IOException e) {
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
}
