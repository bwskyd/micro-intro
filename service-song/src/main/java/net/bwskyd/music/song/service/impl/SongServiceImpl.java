package net.bwskyd.music.song.service.impl;

import exception.BadParameterException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.bwskyd.music.entity.entity.Song;
import net.bwskyd.music.song.mapper.SongDTOMapper;
import net.bwskyd.music.song.repository.SongRepository;
import net.bwskyd.music.song.service.SongService;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.song.SongCreateResponseDTO;
import net.rewerk.music.dto.response.song.SongDeleteResponseDTO;
import net.rewerk.music.dto.response.song.SongResponseDTO;
import org.springframework.stereotype.Service;
import util.Util;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {
    private final SongRepository songRepository;
    private final SongDTOMapper songDTOMapper;

    @Override
    public SongCreateResponseDTO save(SongCreateRequestDTO createDTO) {
        if (songRepository.existsById(createDTO.getId())) {
            throw new EntityExistsException("Song metadata with ID %d already exists".formatted(createDTO.getId()));
        }
        Song song = songDTOMapper.createDTOToSong(createDTO);
        song = songRepository.save(song);
        return SongCreateResponseDTO.builder()
                .id(song.getId())
                .build();
    }

    @Override
    public SongResponseDTO findById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song metadata with ID %d not found".formatted(id)));
        return songDTOMapper.toDTO(song);
    }

    @Override
    public SongDeleteResponseDTO deleteAll(String idsString) {
        if (idsString.length() > 200) {
            throw new BadParameterException("ID parameter length can not be more than 200 characters");
        }
        List<Long> ids = Util.parseIdsFromString(idsString, ",");
        songRepository.deleteAllById(ids);
        return SongDeleteResponseDTO.builder()
                .ids(ids)
                .build();
    }
}
