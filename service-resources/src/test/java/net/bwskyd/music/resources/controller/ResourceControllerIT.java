package net.bwskyd.music.resources.controller;

import exception.ResourceCreateException;
import jakarta.persistence.EntityNotFoundException;
import net.bwskyd.music.resources.service.FilesService;
import net.rewerk.music.dto.internal.files.FileDownloadResultDTO;
import net.rewerk.music.dto.response.resource.ResourceCreateResponseDTO;
import net.rewerk.music.dto.response.resource.ResourcesDeleteResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResourceController.class)
public class ResourceControllerIT {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FilesService filesService;

    private List<String> allowedMimes;
    private MockMultipartFile file;

    @BeforeEach
    public void setup() {
        allowedMimes = List.of("audio/mpeg");
        file = new MockMultipartFile(
                "file",
                "file.mp3",
                "audio/mpeg",
                "file.mp3".getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    void givenIncorrectFile_whenUploadFile_thenErrorResponse() throws Exception {
        when(filesService.save(any(MockMultipartFile.class), eq(allowedMimes))).thenThrow(ResourceCreateException.class);

        mockMvc.perform(multipart("/resources").file(file))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.id").doesNotExist(),
                        jsonPath("$.errorCode").value(400),
                        jsonPath("$.errorMessage").value("Bad Request"),
                        jsonPath("$.errors.length()").value(1)
                );
    }

    @Test
    void givenCorrectFile_whenUploadFile_thenCreateSuccess() throws Exception {
        ResourceCreateResponseDTO resultDTO = new ResourceCreateResponseDTO();
        resultDTO.setId(1L);

        when(filesService.save(eq(file), eq(allowedMimes))).thenReturn(resultDTO);

        mockMvc.perform(multipart("/resources").file(file))
                .andExpectAll(
                        status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.id").value(1),
                        jsonPath("$.errorCode").doesNotExist(),
                        jsonPath("$.errorMessage").doesNotExist(),
                        jsonPath("$.errors").doesNotExist()
                );
    }

    @Test
    void givenInvalidResourceID_whenDownloadFile_thenErrorResponse() throws Exception {
        when(filesService.downloadFileById(eq(1L))).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/resources/1"))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.errorCode").value(404),
                        jsonPath("$.errorMessage").value("Not Found")
                );
    }

    @Test
    void givenValidResourceID_whenDownloadFile_thenSuccess() throws Exception {
        FileDownloadResultDTO resultDTO = FileDownloadResultDTO.builder()
                .filename("filename")
                .filetype("audio/mpeg")
                .bytes(new byte[0])
                .build();

        when(filesService.downloadFileById(eq(1L))).thenReturn(resultDTO);

        mockMvc.perform(get("/resources/1"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType("audio/mpeg"),
                        content().bytes(new byte[0]),
                        header().exists("Content-Disposition"),
                        header().string("Content-Disposition", "attachment; filename=\"filename\"")
                );
    }

    @Test
    void givenIdsString_whenDeleteFiles_thenSuccess() throws Exception {
        String idsString = "1,2,3";
        ResourcesDeleteResponseDTO resultDTO = ResourcesDeleteResponseDTO.builder()
                .ids(List.of(1L, 2L, 3L))
                .build();

        when(filesService.deleteAll(eq(idsString))).thenReturn(resultDTO);

        mockMvc.perform(delete("/resources?id=" + idsString))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.errorCode").doesNotExist(),
                        jsonPath("$.errorMessage").doesNotExist(),
                        jsonPath("$.errors").doesNotExist(),
                        jsonPath("$.ids.length()").value(3)
                );
    }
}
