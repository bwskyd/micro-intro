package net.bwskyd.music.resourceservice.service;


import net.bwskyd.music.resourceservice.dto.internal.parser.MP3ParseResultDTO;

import java.io.IOException;

public interface MP3MetadataService {
    MP3ParseResultDTO parse(byte[] bytes) throws IOException;
}
