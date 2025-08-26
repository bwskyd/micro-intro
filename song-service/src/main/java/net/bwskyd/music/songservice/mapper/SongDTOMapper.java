package net.bwskyd.music.songservice.mapper;

import net.bwskyd.music.songservice.dto.request.song.SongCreateRequestDTO;
import net.bwskyd.music.songservice.dto.response.song.SongResponseDTO;
import net.bwskyd.music.songservice.entity.Song;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SongDTOMapper {
    Song createDTOToSong(SongCreateRequestDTO dto);

    SongResponseDTO toDTO(Song song);
}
