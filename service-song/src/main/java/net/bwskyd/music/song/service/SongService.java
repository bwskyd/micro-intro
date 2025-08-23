package net.bwskyd.music.song.service;

import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import net.rewerk.music.dto.response.song.SongDeleteResponseDTO;
import net.rewerk.music.dto.response.song.SongResponseDTO;

public interface SongService {
    SongCreateResponseDTO save(SongCreateRequestDTO createDTO);

    SongResponseDTO findById(Long id);

    SongDeleteResponseDTO deleteAll(String ids);
}
