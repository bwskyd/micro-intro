package net.bwskyd.music.resources.service;

import exception.MetadataParseException;
import net.bwskyd.music.resources.service.impl.MP3MetadataServiceImpl;
import net.rewerk.music.dto.internal.parser.MP3ParseResultDTO;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MP3MetadataServiceTests {
    private final Path filepath = Path.of("/");
    MP3MetadataServiceImpl mp3MetadataService;

    @BeforeEach
    public void setup() {
        mp3MetadataService = Mockito.spy(new MP3MetadataServiceImpl());
    }

    @Test
    void givenFile_whenParse_thenThrownCannotReadException() {
        try (MockedStatic<AudioFileIO> mocked = Mockito.mockStatic(AudioFileIO.class)) {
            mocked.when(() -> AudioFileIO.read(any(File.class)))
                    .thenThrow(new CannotReadException("CannotReadException"));

            assertThrows(MetadataParseException.class,
                    () -> mp3MetadataService.parse(filepath));
        }
    }

    @Test
    void givenFile_whenParse_thenThrownIOException() {
        try (MockedStatic<AudioFileIO> mocked = Mockito.mockStatic(AudioFileIO.class)) {
            mocked.when(() -> AudioFileIO.read(any(File.class)))
                    .thenThrow(new IOException("IOException"));

            assertThrows(MetadataParseException.class,
                    () -> mp3MetadataService.parse(filepath));
        }
    }

    @Test
    void givenFile_whenParse_thenThrownTagException() {
        try (MockedStatic<AudioFileIO> mocked = Mockito.mockStatic(AudioFileIO.class)) {
            mocked.when(() -> AudioFileIO.read(any(File.class)))
                    .thenThrow(new TagException("TagException"));

            assertThrows(MetadataParseException.class,
                    () -> mp3MetadataService.parse(filepath));
        }
    }

    @Test
    void givenFile_whenParse_thenThrownReadOnlyFileException() {
        try (MockedStatic<AudioFileIO> mocked = Mockito.mockStatic(AudioFileIO.class)) {
            mocked.when(() -> AudioFileIO.read(any(File.class)))
                    .thenThrow(new ReadOnlyFileException("ReadOnlyFileException"));

            assertThrows(MetadataParseException.class,
                    () -> mp3MetadataService.parse(filepath));
        }
    }

    @Test
    void givenFile_whenParse_thenThrownInvalidAudioFrameException() {
        try (MockedStatic<AudioFileIO> mocked = Mockito.mockStatic(AudioFileIO.class)) {
            mocked.when(() -> AudioFileIO.read(any(File.class)))
                    .thenThrow(new InvalidAudioFrameException("InvalidAudioFrameException"));

            assertThrows(MetadataParseException.class,
                    () -> mp3MetadataService.parse(filepath));
        }
    }

    @Test
    void givenFile_whenParse_thenSuccess() {
        MP3ParseResultDTO resultDTO = MP3ParseResultDTO.builder()
                .name("title")
                .album("album")
                .artist("artist")
                .year(2000)
                .duration("02:32")
                .build();
        AudioFile audioFile = Mockito.mock(AudioFile.class);
        AudioHeader  audioHeader = Mockito.mock(AudioHeader.class);
        Tag tag = Mockito.mock(Tag.class);
        doReturn("2000").when(mp3MetadataService).getTagValue(eq(tag), eq(FieldKey.YEAR));
        doReturn("title").when(mp3MetadataService).getTagValue(eq(tag), eq(FieldKey.TITLE));
        doReturn("album").when(mp3MetadataService).getTagValue(eq(tag), eq(FieldKey.ALBUM));
        doReturn("artist").when(mp3MetadataService).getTagValue(eq(tag), eq(FieldKey.ARTIST));

        when(audioFile.getTag()).thenReturn(tag);
        when(audioFile.getAudioHeader()).thenReturn(audioHeader);
        when(audioHeader.getTrackLength()).thenReturn(152);

        try (MockedStatic<AudioFileIO> mocked = Mockito.mockStatic(AudioFileIO.class)) {
            mocked.when(() -> AudioFileIO.read(any(File.class)))
                    .thenReturn(audioFile);

            assertEquals(resultDTO, mp3MetadataService.parse(filepath));

            verify(mp3MetadataService, times(4)).getTagValue(eq(tag), any(FieldKey.class));
        }
    }
}
