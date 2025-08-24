package net.bwskyd.music.resources.feign;

import net.bwskyd.music.resources.feign.config.DefaultConfiguration;
import net.bwskyd.music.resources.feign.fallback.SongsFeignClientFallback;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import net.rewerk.music.dto.response.song.SongDeleteResponseDTO;
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
