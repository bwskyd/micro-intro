package net.bwskyd.music.song.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.bwskyd.music.song.service.SongService;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import net.rewerk.music.dto.response.song.SongDeleteResponseDTO;
import net.rewerk.music.dto.response.song.SongResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {
    private final SongService songService;

    @PostMapping
    public ResponseEntity<SongCreateResponseDTO> createSong(
            @Valid @RequestBody SongCreateRequestDTO requestDTO,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        SongCreateResponseDTO result = songService.save(requestDTO);
        return ResponseEntity.created(uriComponentsBuilder
                        .replacePath("/songs/{id}")
                        .build(Map.of("id", result.getId()))
                )
                .body(result);
    }

    @GetMapping("{id:\\d+}")
    public ResponseEntity<SongResponseDTO> getSongById(@PathVariable Long id) {
        return ResponseEntity.ok().body(songService.findById(id));
    }

    @DeleteMapping
    public ResponseEntity<SongDeleteResponseDTO> deleteSongs(@RequestParam String id) {
        return ResponseEntity.ok().body(songService.deleteAll(id));
    }
}
