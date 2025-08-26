package net.bwskyd.music.resourceservice.config.advice;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import net.bwskyd.music.resourceservice.dto.response.exception.ExceptionResponseDTO;
import net.bwskyd.music.resourceservice.dto.response.exception.ValidationExceptionResponseDTO;
import net.bwskyd.music.resourceservice.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MethodArgumentNotValidException.class
    })
    public final ResponseEntity<ValidationExceptionResponseDTO> handleException(Exception ex) {
        ValidationExceptionResponseDTO response = new ValidationExceptionResponseDTO(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "Validation error"
        );
        if (ex instanceof MethodArgumentNotValidException ex1) {
            final List<FieldError> errors = ex1.getBindingResult().getFieldErrors();
            for (FieldError error : errors) {
                response.put(
                        error.getField(),
                        (error.getCodes() != null && Arrays.asList(error.getCodes()).contains("typeMismatch"))
                                ? "Invalid type"
                                : error.getDefaultMessage()
                );
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({
            BadRequestException.class,
            HttpMessageNotReadableException.class,
            MetadataParseException.class,
            BadParameterException.class,
            InvalidFileException.class,
            ResourceCreateException.class,
            CSVParseException.class
    })
    public final ResponseEntity<ExceptionResponseDTO> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponseDTO(
                        getErrorMessageORDefault(HttpStatus.BAD_REQUEST, ex),
                        String.valueOf(HttpStatus.BAD_REQUEST.value())
                )
        );
    }

    @ExceptionHandler({
            HttpMediaTypeNotSupportedException.class
    })
    public final ResponseEntity<ExceptionResponseDTO> handleUnsupportedMediaTypeException(
            HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ExceptionResponseDTO(
                "Invalid file format: %s. Only MP3 files are allowed"
                        .formatted(ex.getContentType()),
                        String.valueOf(HttpStatus.BAD_REQUEST.value())
                )
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({
            SongCreateException.class,
            SongDeleteException.class
    })
    public final ResponseEntity<ExceptionResponseDTO> handleException(SongDeleteException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResponseDTO(
                        getErrorMessageORDefault(HttpStatus.INTERNAL_SERVER_ERROR, ex),
                        String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        )
                );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            EntityNotFoundException.class,
            FileNotFoundException.class,
            NotFoundException.class
    })
    public final ResponseEntity<ExceptionResponseDTO> handleNotFoundException(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponseDTO(
                getErrorMessageORDefault(HttpStatus.NOT_FOUND, ex),
                String.valueOf(HttpStatus.NOT_FOUND.value())
            )
        );
    }

    @ExceptionHandler({
            EntityExistsException.class
    })
    public final ResponseEntity<ExceptionResponseDTO> handleConflictException(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ExceptionResponseDTO(
                        getErrorMessageORDefault(HttpStatus.CONFLICT, ex),
                        String.valueOf(HttpStatus.CONFLICT.value())
                        )
                );
    }

    private String getErrorMessageORDefault(HttpStatus httpStatus, @NotNull Throwable ex) {
        String message = ex.getMessage();
        return message == null ? httpStatus.getReasonPhrase() : message;
    }
}
