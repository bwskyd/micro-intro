package net.bwskyd.music.resourceservice.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SongCreateResponseDTO {
    private Long id;
}
