package net.rewerk.music.dto.response.exception;

import lombok.Getter;

@Getter
public class EntityExistsExceptionResponseDTO {
    private final int errorCode;
    private final String errorMessage;

    public EntityExistsExceptionResponseDTO(String message) {
        this.errorCode = 409;
        this.errorMessage = message;
    }
}
