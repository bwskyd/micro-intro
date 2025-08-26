package net.bwskyd.music.resourceservice.feign.fallback;

import lombok.extern.slf4j.Slf4j;
import net.bwskyd.music.resourceservice.dto.request.song.SongCreateRequestDTO;
import net.bwskyd.music.resourceservice.dto.response.song.SongCreateResponseDTO;
import net.bwskyd.music.resourceservice.dto.response.song.SongDeleteResponseDTO;
import net.bwskyd.music.resourceservice.exception.SongCreateException;
import net.bwskyd.music.resourceservice.exception.SongDeleteException;
import net.bwskyd.music.resourceservice.feign.SongsFeignClient;
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
