package com.desafio.dev.config;

import com.desafio.dev.domain.exception.UseCaseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlingController {

    private final static String INVALID_REQUEST_MESSAGE = "Revise sua requisição";

    @ExceptionHandler({UseCaseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUseCaseException(final UseCaseException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler({BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBindException(final BindException e) {
        List<ObjectError> errors = e.getAllErrors();

        ErrorResponse response = new ErrorResponse();
        response.setMessage("Campos da sua requisição estão errados");
        for (ObjectError error : errors) {
            response.addError(error.getDefaultMessage());
        }

        return response;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCustomFieldsValidationException(final MethodArgumentNotValidException e) {
        final ErrorResponse response = new ErrorResponse();
        response.setMessage(INVALID_REQUEST_MESSAGE);
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            response.addError(error.getDefaultMessage());
        }
        return response;
    }

    @ExceptionHandler({InvalidFormatException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleParseException(final InvalidFormatException exception) {

        final ErrorResponse errorResponse = ErrorResponse.builder()
                .message(INVALID_REQUEST_MESSAGE)
                .build();

        errorResponse.addError(exception.getMessage());

        return errorResponse;
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException exception) {

        final ErrorResponse errorResponse = ErrorResponse.builder()
                .message(INVALID_REQUEST_MESSAGE)
                .build();

        errorResponse.addError(exception.getMessage());

        return errorResponse;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        final ErrorResponse response = new ErrorResponse();
        response.setMessage("Revise o corpo da sua requisição");
        return response;
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericError(Exception e) {
        log.error("Erro interno", e);
        return ErrorResponse.builder()
                .message("Erro interno")
                .build();
    }

    @ExceptionHandler({ClassNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final ClassNotFoundException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
                .build();
    }
}