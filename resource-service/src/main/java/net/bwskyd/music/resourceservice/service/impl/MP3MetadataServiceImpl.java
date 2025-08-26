package net.bwskyd.music.resourceservice.service.impl;

import net.bwskyd.music.resourceservice.dto.internal.parser.MP3ParseResultDTO;
import net.bwskyd.music.resourceservice.exception.MetadataParseException;
import net.bwskyd.music.resourceservice.service.MP3MetadataService;
import net.bwskyd.music.resourceservice.util.Util;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class MP3MetadataServiceImpl implements MP3MetadataService {
    @Override
    public MP3ParseResultDTO parse(byte[] bytes) {
        Path tempFile;
        File mp3File;
        AudioFile audioFile;
        Tag tag;
        try {
            tempFile = Files.createTempFile(UUID.randomUUID() + "_tempFile", ".mp3");
            Files.write(tempFile, bytes);
            mp3File = new File(tempFile.toFile().getAbsolutePath());
        } catch (IOException e) {
            throw new MetadataParseException("Failed to create temporary MP3 File");
        }
        try {
            audioFile = AudioFileIO.read(mp3File);
            tag = audioFile.getTag();
        } catch (CannotReadException | IOException e) {
            throw new MetadataParseException("Failed to read metadata");
        } catch (TagException e) {
            throw new MetadataParseException("Tag exception in metadata");
        } catch (ReadOnlyFileException e) {
            throw new MetadataParseException("Metadata in read only mode");
        } catch (InvalidAudioFrameException e) {
            throw new MetadataParseException("Invalid audio frame");
        }
        return MP3ParseResultDTO.builder()
                .name(getTagValue(tag, FieldKey.TITLE))
                .album(getTagValue(tag, FieldKey.ALBUM))
                .artist(getTagValue(tag, FieldKey.ARTIST))
                .year(getTagValue(tag, FieldKey.YEAR))
                .duration(Util.secondsToMMSS(audioFile.getAudioHeader().getTrackLength()))
                .build();
    }

    public String getTagValue(Tag tag, FieldKey field) {
        return tag.getFirst(field);
    }
}
