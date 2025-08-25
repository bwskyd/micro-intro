package net.bwskyd.music.resources.feign.fallback;

import exception.SongCreateException;
import exception.SongDeleteException;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SongsFeignClientFallbackTest {
    SongsFeignClientFallback songsFeignClientFallback = new SongsFeignClientFallback();

    @Test
    void whenCreateSong_thenThrowException() {
        Exception ex = assertThrows(SongCreateException.class,
                () -> songsFeignClientFallback.createSong(new SongCreateRequestDTO()));
        assertEquals("Failed to create song metadata", ex.getMessage());
    }

    @Test
    void whenDeleteSong_thenThrowException() {
        Exception ex = assertThrows(SongDeleteException.class,
                () -> songsFeignClientFallback.deleteSongs(""));
        assertEquals("Failed to delete songs", ex.getMessage());
    }
}
