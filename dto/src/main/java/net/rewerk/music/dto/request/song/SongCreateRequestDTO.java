package net.rewerk.music.dto.request.song;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SongCreateRequestDTO {
    @NotNull(message = "ID parameter required")
    @Positive(message = "ID should be positive number")
    private Long id;
    @NotNull(message = "Name parameter required")
    @Length(min = 1,
            max = 100,
            message = "Name parameter length should be in range {min}-{max} characters")
    private String name;
    @NotNull(message = "Artist parameter required")
    @Length(min = 1,
            max = 100,
            message = "Artist parameter length should be in range {min}-{max} characters")
    private String artist;
    @NotNull(message = "Album parameter required")
    @Length(min = 1,
            max = 100,
            message = "Album parameter length should be in range {min}-{max} characters")
    private String album;
    @NotNull(message = "Duration parameter required")
    @Pattern(regexp = "^([0-9]{2}):([0-5][0-9])$",
            message = "Invalid duration parameter format")
    private String duration;
    @NotNull(message = "Year parameter required")
    @Min(value = 1900,
            message = "Year parameter can not be less than 1900")
    @Max(value = 2099,
            message = "Year parameter can not be more than 2099")
    private Integer year;
}
