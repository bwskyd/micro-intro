package net.bwskyd.music.resources.service;

import net.rewerk.music.dto.internal.files.FileDownloadResultDTO;
import net.rewerk.music.dto.response.resource.ResourceCreateResponseDTO;
import net.rewerk.music.dto.response.resource.ResourcesDeleteResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FilesService {
    ResourceCreateResponseDTO save(MultipartFile file, List<String> acceptTypes);

    FileDownloadResultDTO downloadFileById(Long id);

    ResourcesDeleteResponseDTO deleteAll(String ids);
}
