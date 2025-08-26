package net.bwskyd.music.songservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "songs_t")
public class Song {
    @Id
    private Long id;
    @Column(name = "name_c", nullable = false)
    private String name;
    @Column(name = "artist_c", nullable = false)
    private String artist;
    @Column(name = "album_c", nullable = false)
    private String album;
    @Column(name = "duration_c", nullable = false)
    private String duration;
    @Column(name = "year_c", nullable = false)
    private Integer year;
}
