package net.bwskyd.music.song.mapper;

import net.bwskyd.music.entity.entity.Song;
import net.rewerk.music.dto.request.song.SongCreateRequestDTO;
import net.rewerk.music.dto.response.song.SongResponseDTO;
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
