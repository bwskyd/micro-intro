package net.bwskyd.music.song.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bwskyd.music.song.service.SongService;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SongController.class)
public class SongControllerIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    private SongService songService;

    @Test
    void givenIncorrectDTO_whenCreateSong_thenThrowException() throws Exception {
        SongCreateRequestDTO requestDTO = SongCreateRequestDTO.builder()
                .id(0L)
                .name("")
                .artist("")
                .album("")
                .duration("")
                .year(0)
                .build();
        String jsonBody = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)
        ).andExpectAll(
                status().isBadRequest(),
                jsonPath("$.id").doesNotExist(),
                jsonPath("$.errorCode").value(400),
                jsonPath("$.errorMessage").value("Bad Request"),
                jsonPath("$.errors.length()").value(6)
        );
    }
}
