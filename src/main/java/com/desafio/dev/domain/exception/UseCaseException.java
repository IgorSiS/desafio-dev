package com.desafio.dev.domain.exception;

public class UseCaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UseCaseException() {}

    public UseCaseException(String message) {
        super(message);
    }

    public UseCaseException(String message, Throwable cause) {
        super(message, cause);
    }
}