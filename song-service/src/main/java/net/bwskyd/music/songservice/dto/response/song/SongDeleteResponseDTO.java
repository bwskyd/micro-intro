package net.bwskyd.music.songservice.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongDeleteResponseDTO {
    private List<Long> ids;
}
