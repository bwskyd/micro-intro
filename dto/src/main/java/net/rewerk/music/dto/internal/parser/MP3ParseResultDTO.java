package net.rewerk.music.dto.internal.parser;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class MP3ParseResultDTO {
    private String name;
    private String artist;
    private String album;
    private String duration;
    private Integer year;
}
