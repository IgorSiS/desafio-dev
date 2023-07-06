package com.desafio.dev.domain.exception;

import java.io.Serial;

public class UseCaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UseCaseException() {}

    public UseCaseException(String message) {
        super(message);
    }

    public UseCaseException(String message, Throwable cause) {
        super(message, cause);
    }
}