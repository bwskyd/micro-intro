package net.bwskyd.music.resourceservice.dto.request.song;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongCreateRequestDTO {
    @NotNull(message = "ID required")
    @Positive(message = "ID must be positive number")
    private Long id;
    @NotNull(message = "Name required")
    @Length(min = 1,
            max = 100,
            message = "Name length must be in range {min}-{max} characters")
    private String name;
    @NotNull(message = "Artist required")
    @Length(min = 1,
            max = 100,
            message = "Artist length must be in range {min}-{max} characters")
    private String artist;
    @NotNull(message = "Album required")
    @Length(min = 1,
            max = 100,
            message = "Album length must be in range {min}-{max} characters")
    private String album;
    @NotNull(message = "Duration required")
    @Pattern(regexp = "^([0-9]{2}):([0-5][0-9])$",
            message = "Duration must be in mm:ss format with leading zeros")
    private String duration;
    @Pattern(
            regexp = "^(19\\d{2}|20\\d{2}|2099)$",
            message = "Year must be in format YYYY and between 1900 and 2099"
    )
    private String year;
}
