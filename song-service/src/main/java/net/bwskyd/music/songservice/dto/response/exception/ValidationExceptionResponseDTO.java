package net.bwskyd.music.songservice.dto.response.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import net.bwskyd.music.songservice.config.advice.serializer.DetailsSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ValidationExceptionResponseDTO {
    private final String errorMessage;
    @JsonSerialize(using = DetailsSerializer.class)
    private final Map<String, List<String>> details;
    private final String errorCode;

    public ValidationExceptionResponseDTO(String code, String message) {
        this.errorCode = code;
        this.errorMessage = message;
        this.details = new HashMap<>();
    }

    public void put(String field, String message) {
        details.computeIfAbsent(field, f -> new ArrayList<>()).add(message);
    }
}
