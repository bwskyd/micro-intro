package net.bwskyd.music.songservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.bwskyd.music.songservice.dto.request.song.SongCreateRequestDTO;
import net.bwskyd.music.songservice.dto.response.song.SongCreateResponseDTO;
import net.bwskyd.music.songservice.dto.response.song.SongDeleteResponseDTO;
import net.bwskyd.music.songservice.dto.response.song.SongResponseDTO;
import net.bwskyd.music.songservice.service.SongService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {
    private final SongService songService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SongCreateResponseDTO> createSong(
            @Valid @RequestBody SongCreateRequestDTO requestDTO
    ) {
        SongCreateResponseDTO result = songService.save(requestDTO);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SongResponseDTO> getSongById(@PathVariable String id) {
        return ResponseEntity.ok().body(songService.findById(id));
    }

    @DeleteMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SongDeleteResponseDTO> deleteSongs(@RequestParam String id) {
        return ResponseEntity.ok().body(songService.deleteAll(id));
    }
}
