package net.rewerk.music.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongResponseDTO {
    private Long id;
    private String name;
    private String artist;
    private String album;
    private String duration;
    private Integer year;
}
