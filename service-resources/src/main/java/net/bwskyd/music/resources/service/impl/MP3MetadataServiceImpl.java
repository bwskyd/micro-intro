package net.bwskyd.music.resources.service.impl;

import exception.MetadataParseException;
import net.bwskyd.music.resources.service.MP3MetadataService;
import net.rewerk.music.dto.internal.parser.MP3ParseResultDTO;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class MP3MetadataServiceImpl implements MP3MetadataService {
    @Override
    public MP3ParseResultDTO parse(Path filepath) throws IOException {
        File mp3File = new File(filepath.toUri());
        AudioFile audioFile;
        Tag tag;
        try {
            audioFile = AudioFileIO.read(mp3File);
            tag = audioFile.getTag();
        } catch (CannotReadException e) {
            throw new MetadataParseException("Failed to read metadata");
        } catch (TagException e) {
            throw new MetadataParseException("Tag exception in metadata");
        } catch (ReadOnlyFileException e) {
            throw new MetadataParseException("Metadata in read only mode");
        } catch (InvalidAudioFrameException e) {
            throw new MetadataParseException("Invalid audio frame");
        }
        String year = tag.getFirst(FieldKey.YEAR);
        return MP3ParseResultDTO.builder()
                .name(tag.getFirst(FieldKey.TITLE))
                .album(tag.getFirst(FieldKey.ALBUM))
                .artist(tag.getFirst(FieldKey.ARTIST))
                .year(year == null ? 0 : Integer.parseInt(year))
                .duration(String.valueOf(audioFile.getAudioHeader().getTrackLength()))
                .build();
    }
}
