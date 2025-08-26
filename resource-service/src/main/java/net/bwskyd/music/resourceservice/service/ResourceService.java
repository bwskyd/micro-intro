package net.bwskyd.music.resourceservice.service;

import net.bwskyd.music.resourceservice.dto.response.resource.ResourceCreateResponseDTO;
import net.bwskyd.music.resourceservice.dto.response.resource.ResourcesDeleteResponseDTO;

public interface ResourceService {
    ResourceCreateResponseDTO save(byte[] bytes, String contentType);

    byte[] downloadById(String id);

    ResourcesDeleteResponseDTO deleteByIds(String ids);
}
