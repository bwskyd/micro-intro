package net.bwskyd.music.songservice.service;


import net.bwskyd.music.songservice.dto.request.song.SongCreateRequestDTO;
import net.bwskyd.music.songservice.dto.response.song.SongCreateResponseDTO;
import net.bwskyd.music.songservice.dto.response.song.SongDeleteResponseDTO;
import net.bwskyd.music.songservice.dto.response.song.SongResponseDTO;

public interface SongService {
    SongCreateResponseDTO save(SongCreateRequestDTO createDTO);

    SongResponseDTO findById(String id);

    SongDeleteResponseDTO deleteAll(String ids);
}
