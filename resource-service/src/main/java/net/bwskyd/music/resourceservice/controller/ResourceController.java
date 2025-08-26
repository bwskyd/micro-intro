package net.bwskyd.music.resourceservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.bwskyd.music.resourceservice.dto.response.resource.ResourceCreateResponseDTO;
import net.bwskyd.music.resourceservice.dto.response.resource.ResourcesDeleteResponseDTO;
import net.bwskyd.music.resourceservice.service.ResourceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {
    private final String AUDIO_MIME_TYPE = "audio/mpeg";
    private final ResourceService resourceService;

    @PostMapping(
            consumes = AUDIO_MIME_TYPE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResourceCreateResponseDTO> uploadResource(@RequestBody byte[] bytes,
                                                                    HttpServletRequest request) {
        ResourceCreateResponseDTO result = resourceService.save(bytes, request.getContentType());
        return ResponseEntity.ok().body(result);
    }

    @GetMapping(
            path = "/{id}",
            produces = AUDIO_MIME_TYPE
    )
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id) {
        byte[] bytes = resourceService.downloadById(id);
        return ResponseEntity.ok()
                .contentLength(bytes.length)
                .body(bytes);
    }

    @DeleteMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResourcesDeleteResponseDTO> deleteFiles(@RequestParam String id) {
        return ResponseEntity.ok().body(resourceService.deleteByIds(id));
    }
}
