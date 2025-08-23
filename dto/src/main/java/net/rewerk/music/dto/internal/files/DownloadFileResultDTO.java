package net.rewerk.music.dto.internal.files;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadFileResultDTO {
    private byte[] bytes;
    private String filename;
    private String filetype;
}
