package net.bwskyd.music.songservice.service.impl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.bwskyd.music.songservice.dto.request.song.SongCreateRequestDTO;
import net.bwskyd.music.songservice.dto.response.song.SongCreateResponseDTO;
import net.bwskyd.music.songservice.dto.response.song.SongDeleteResponseDTO;
import net.bwskyd.music.songservice.dto.response.song.SongResponseDTO;
import net.bwskyd.music.songservice.entity.Song;
import net.bwskyd.music.songservice.exception.BadParameterException;
import net.bwskyd.music.songservice.exception.BadRequestException;
import net.bwskyd.music.songservice.mapper.SongDTOMapper;
import net.bwskyd.music.songservice.repository.SongRepository;
import net.bwskyd.music.songservice.service.SongService;
import net.bwskyd.music.songservice.util.CSVUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {
    private final SongRepository songRepository;
    private final SongDTOMapper songDTOMapper;

    @Override
    public SongCreateResponseDTO save(SongCreateRequestDTO createDTO) {
        if (songRepository.existsById(createDTO.getId())) {
            throw new EntityExistsException("Metadata for resource ID=%s already exists".formatted(createDTO.getId()));
        }
        Song song = songDTOMapper.createDTOToSong(createDTO);
        song = songRepository.save(song);
        return SongCreateResponseDTO.builder()
                .id(song.getId())
                .build();
    }

    @Override
    public SongResponseDTO findById(String id) {
        if (id.matches(".*\\p{L}.*|.*\\d+[.,]\\d+.*")) {
            throw new BadRequestException("Invalid value '%s' for ID. Must be a positive integer"
                    .formatted(id));
        }
        long identifier = Long.parseLong(id.trim());
        if (identifier <= 0) {
            throw new BadRequestException("Invalid value '%d' for ID. Must be a positive integer"
                    .formatted(identifier));
        }
        Song song = songRepository.findById(identifier)
                .orElseThrow(() -> new EntityNotFoundException("Song metadata for ID=%s not found"
                        .formatted(id)));
        return songDTOMapper.toDTO(song);
    }

    @Override
    public SongDeleteResponseDTO deleteAll(String idsString) {
        if (idsString.length() > 200) {
            throw new BadParameterException("CSV string is too long: received 208 characters, maximum allowed is 200");
        }
        List<Long> ids = CSVUtil.parseIDs(idsString, ',');
        List<Song> songs = songRepository.findAllById(ids);
        List<Long> idsToDelete = songs.stream().map(Song::getId).toList();
        if (!idsToDelete.isEmpty()) {
            songRepository.deleteAllById(ids);
        }
        return SongDeleteResponseDTO.builder()
                .ids(idsToDelete)
                .build();
    }
}
