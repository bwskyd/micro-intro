package net.bwskyd.music.resources.feign;

import com.github.tomakehurst.wiremock.WireMockServer;
import net.bwskyd.music.resources.config.WireMockConfig;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import net.rewerk.music.dto.response.song.SongDeleteResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.*;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static net.bwskyd.music.resources.feign.mocks.SongsMocks.setupMockBooksResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = {
                SongsFeignClient.class,
                WireMockConfig.class
        }
)
@EnableConfigurationProperties
@EnableFeignClients(
        clients = SongsFeignClient.class
)
@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(classes = {
        FeignAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
})
@Import({
        WireMockConfig.class
})
@ActiveProfiles("test")
class SongsFeignClientIT {

    @Autowired
    private WireMockServer mockSongsService;

    @Autowired
    private SongsFeignClient songsFeignClient;

    @BeforeEach
    void setUp() {
        setupMockBooksResponse(mockSongsService);
    }

    @Test
    void whenCreateSong_thenSuccess() {
        SongCreateResponseDTO responseDTO = new SongCreateResponseDTO();
        responseDTO.setId(1L);

        assertEquals(responseDTO, songsFeignClient.createSong(new SongCreateRequestDTO()));
    }

    @Test
    void whenDeleteSongs_thenSuccess() {
        SongDeleteResponseDTO responseDTO = new SongDeleteResponseDTO();
        responseDTO.setIds(List.of(1L, 2L, 3L));

        assertEquals(responseDTO, songsFeignClient.deleteSongs("1,2,3"));
    }
}
