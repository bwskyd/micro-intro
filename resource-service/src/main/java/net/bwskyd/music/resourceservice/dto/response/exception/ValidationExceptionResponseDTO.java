package net.bwskyd.music.resourceservice.dto.response.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ValidationExceptionResponseDTO {
    private final String errorCode;
    private final String errorMessage;
    private final Map<String, List<String>> errors;

    public ValidationExceptionResponseDTO(String code, String message) {
        this.errorCode = code;
        this.errorMessage = message;
        this.errors = new HashMap<>();
    }

    public void put(String field, String message) {
        errors.computeIfAbsent(field, f -> new ArrayList<>()).add(message);
    }
}
