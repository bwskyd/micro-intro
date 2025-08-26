package net.bwskyd.music.resourceservice.feign;

import net.bwskyd.music.resourceservice.dto.request.song.SongCreateRequestDTO;
import net.bwskyd.music.resourceservice.dto.response.song.SongCreateResponseDTO;
import net.bwskyd.music.resourceservice.dto.response.song.SongDeleteResponseDTO;
import net.bwskyd.music.resourceservice.feign.config.DefaultConfiguration;
import net.bwskyd.music.resourceservice.feign.fallback.SongsFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "songs-client",
        url = "${services.songs.base_uri}",
        configuration = {
                DefaultConfiguration.class
        },
        fallback = SongsFeignClientFallback.class
)
public interface SongsFeignClient {
    @PostMapping
    SongCreateResponseDTO createSong(SongCreateRequestDTO requestDTO);

    @DeleteMapping
    SongDeleteResponseDTO deleteSongs(@RequestParam String id);
}
