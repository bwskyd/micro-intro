package net.rewerk.music.dto.response.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundExceptionResponseDTO {
    private final int errorCode;
    private final String errorMessage;

    public EntityNotFoundExceptionResponseDTO(String message) {
        this.errorCode = 404;
        this.errorMessage = message;
    }
}
