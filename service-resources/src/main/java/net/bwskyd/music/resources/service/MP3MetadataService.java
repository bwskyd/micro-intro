package net.bwskyd.music.resources.service;

import net.rewerk.music.dto.internal.parser.MP3ParseResultDTO;

import java.io.IOException;
import java.nio.file.Path;

public interface MP3MetadataService {
    MP3ParseResultDTO parse(Path filepath) throws IOException;
}
