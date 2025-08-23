package net.bwskyd.music.resources.service;

import net.rewerk.music.dto.internal.files.DownloadFileResultDTO;
import net.rewerk.music.dto.response.resource.CreateResourceResponseDTO;
import net.rewerk.music.dto.response.resource.DeleteResourcesResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FilesService {
    CreateResourceResponseDTO save(MultipartFile file, List<String> acceptTypes);

    DownloadFileResultDTO downloadFileById(Long id);

    DeleteResourcesResponseDTO deleteAll(String ids);
}
