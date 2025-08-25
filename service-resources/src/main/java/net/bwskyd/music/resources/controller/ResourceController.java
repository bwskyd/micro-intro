package net.bwskyd.music.resources.controller;

import lombok.RequiredArgsConstructor;
import net.bwskyd.music.resources.service.FilesService;
import net.rewerk.music.dto.internal.files.FileDownloadResultDTO;
import net.rewerk.music.dto.response.resource.ResourceCreateResponseDTO;
import net.rewerk.music.dto.response.resource.ResourcesDeleteResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {
    @Value("${uploads.allowedMimes}")
    private List<String> allowedMimes;
    private final FilesService filesService;

    @PostMapping
    public ResponseEntity<ResourceCreateResponseDTO> uploadResource(
            @RequestParam("file") MultipartFile file,
            UriComponentsBuilder uriBuilder
    ) {
        ResourceCreateResponseDTO result = filesService.save(file, allowedMimes);
        return ResponseEntity
                .created(uriBuilder
                        .replacePath("/resources/{resourceId}")
                        .build(Map.of("resourceId", result.getId())
                        )
                )
                .body(result);
    }

    @GetMapping("{id}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable Long id) {
        FileDownloadResultDTO result = filesService.downloadFileById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.getFiletype()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"%s\"".formatted(result.getFilename()))
                .body(new ByteArrayResource(result.getBytes()));
    }

    @DeleteMapping
    public ResponseEntity<ResourcesDeleteResponseDTO> deleteFiles(@RequestParam String id) {
        return ResponseEntity.ok().body(filesService.deleteAll(id));
    }
}
