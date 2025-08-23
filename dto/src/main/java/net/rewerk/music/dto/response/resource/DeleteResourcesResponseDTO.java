package net.rewerk.music.dto.response.resource;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class DeleteResourcesResponseDTO {
    private List<Long> ids;
}
