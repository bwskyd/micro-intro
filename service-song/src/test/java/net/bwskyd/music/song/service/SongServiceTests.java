package net.bwskyd.music.song.service;

import exception.BadParameterException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import net.bwskyd.music.entity.entity.Song;
import net.bwskyd.music.song.mapper.SongDTOMapper;
import net.bwskyd.music.song.repository.SongRepository;
import net.bwskyd.music.song.service.impl.SongServiceImpl;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import net.rewerk.music.dto.response.song.SongDeleteResponseDTO;
import net.rewerk.music.dto.response.song.SongResponseDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import util.Util;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SongServiceTests {
    @Mock
    private SongRepository songRepository;
    @Mock
    private SongDTOMapper songDTOMapper;
    @InjectMocks
    private SongServiceImpl songService;
    SongCreateRequestDTO createDTO;
    Song song;

    @BeforeEach
    public void setUp() {
        song = new Song(1L, "name", "artist", "album", "00:01", 2025);
        createDTO = SongCreateRequestDTO.builder()
                .id(1L)
                .name("name")
                .artist("artist")
                .album("album")
                .duration("00:01")
                .year(2025)
                .build();
    }

    @Test
    void givenExistingId_whenSave_thenThrownException() {
        when(songRepository.existsById(eq(1L))).thenReturn(Boolean.TRUE);

        assertThrows(EntityExistsException.class,
                () -> songService.save(createDTO));

        verify(songRepository, times(1)).existsById(eq(1L));
        verify(songDTOMapper, never()).createDTOToSong(any(SongCreateRequestDTO.class));
        verify(songRepository, never()).save(any(Song.class));
    }

    @Test
    void givenCorrectDTO_whenSave_thenSuccess() {
        SongCreateResponseDTO responseDTO = new SongCreateResponseDTO();
        responseDTO.setId(song.getId());

        when(songRepository.existsById(eq(1L))).thenReturn(Boolean.FALSE);
        when(songDTOMapper.createDTOToSong(eq(createDTO))).thenReturn(song);
        when(songRepository.save(eq(song))).thenReturn(song);

        assertEquals(responseDTO, songService.save(createDTO));

        verify(songRepository, times(1)).existsById(eq(1L));
        verify(songDTOMapper, times(1)).createDTOToSong(any(SongCreateRequestDTO.class));
        verify(songRepository, times(1)).save(any(Song.class));
    }

    @Test
    void givenIncorrectId_whenFindById_thenThrowException() {
        when(songRepository.findById(eq(1L))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> songService.findById(1L));

        verify(songDTOMapper, never()).toDTO(any(Song.class));
    }

    @Test
    void givenCorrectID_whenFindById_thenReturnDTO() {
        SongResponseDTO responseDTO = new SongResponseDTO();
        responseDTO.setId(1L);

        when(songRepository.findById(eq(1L))).thenReturn(Optional.of(song));
        when(songDTOMapper.toDTO(eq(song))).thenReturn(responseDTO);

        assertEquals(responseDTO, songService.findById(1L));

        verify(songRepository, times(1)).findById(eq(1L));
        verify(songDTOMapper, times(1)).toDTO(eq(song));
    }

    @Test
    void givenLongIdsString_whenDeleteSongs_thenThrowException() {
        String idsString = RandomStringUtils.insecure().next(201);

        try (MockedStatic<Util> mocked = mockStatic(Util.class)) {
            mocked.when(() -> Util.parseIdsFromString(idsString, ","))
                    .thenAnswer(i -> null);

            assertThrows(BadParameterException.class,
                    () -> songService.deleteAll(idsString));

            mocked.verify(() -> Util.parseIdsFromString(eq(idsString), eq(",")), never());
            verify(songRepository, never()).deleteAllById(anyList());
        }
    }

    @Test
    void givenCorrectIdsString_whenDeleteSongs_thenReturnDTO() {
        String idsString = "1,2,3";
        List<Long> ids = List.of(1L, 2L, 3L);
        SongDeleteResponseDTO responseDTO = SongDeleteResponseDTO.builder()
                .ids(ids)
                .build();

        try (MockedStatic<Util> mocked = mockStatic(Util.class)) {
            mocked.when(() -> Util.parseIdsFromString(idsString, ","))
                    .thenReturn(ids);

            assertEquals(responseDTO, songService.deleteAll(idsString));

            mocked.verify(() -> Util.parseIdsFromString(eq(idsString), eq(",")), times(1));
            verify(songRepository, times(1)).deleteAllById(eq(ids));
        }
    }
}
