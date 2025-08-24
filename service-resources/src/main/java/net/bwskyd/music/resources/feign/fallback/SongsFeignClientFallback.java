package net.bwskyd.music.resources.feign.fallback;

import exception.SongCreateException;
import exception.SongDeleteException;
import lombok.extern.slf4j.Slf4j;
import net.bwskyd.music.resources.feign.SongsFeignClient;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import net.rewerk.music.dto.response.song.SongDeleteResponseDTO;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SongsFeignClientFallback implements SongsFeignClient {
    @Override
    public SongCreateResponseDTO createSong(SongCreateRequestDTO requestDTO) {
        log.info("SongsFeignClient: fallback for createSong method called with requestDTO: {}", requestDTO);
        throw new SongCreateException("Failed to create song metadata");
    }

    @Override
    public SongDeleteResponseDTO deleteSongs(String id) {
        log.info("SongsFeignClient: fallback for deleteSongs method called with id param: {}", id);
        throw new SongDeleteException("Failed to delete songs");
    }
}
