package com.desafio.dev.exception;

public class GatewayException extends Exception{

    private static final long serialVersionUID = 1L;

    public GatewayException() {}


    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }

}