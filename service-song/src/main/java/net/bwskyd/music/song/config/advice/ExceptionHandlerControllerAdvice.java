package net.bwskyd.music.song.config.advice;

import exception.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import net.rewerk.music.dto.response.exception.EntityExistsExceptionResponseDTO;
import net.rewerk.music.dto.response.exception.EntityNotFoundExceptionResponseDTO;
import net.rewerk.music.dto.response.exception.ValidationExceptionResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
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
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MetadataParseException.class,
            BadParameterException.class,
            BadRequestException.class,
            InvalidFileException.class,
            SongCreateException.class
    })
    public final ResponseEntity<ValidationExceptionResponseDTO> handleException(Exception ex) {
        ValidationExceptionResponseDTO response = new ValidationExceptionResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase()
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
        } else if (ex instanceof HttpMessageNotReadableException) {
            response.put(
                    "error",
                    "Invalid input type"
            );
        } else {
            response.put(
                    "type",
                    ex.getMessage()
            );
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            EntityNotFoundException.class,
            FileNotFoundException.class,
            NotFoundException.class
    })
    public final ResponseEntity<EntityNotFoundExceptionResponseDTO> handleNotFoundException(Exception ex) {
        EntityNotFoundExceptionResponseDTO response = new EntityNotFoundExceptionResponseDTO(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({
            EntityExistsException.class
    })
    public final ResponseEntity<EntityExistsExceptionResponseDTO> handleConflictException(Exception ex) {
        EntityExistsExceptionResponseDTO response = new EntityExistsExceptionResponseDTO(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
