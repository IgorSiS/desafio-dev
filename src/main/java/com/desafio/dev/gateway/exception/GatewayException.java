package com.desafio.dev.gateway.exception;

import java.io.Serial;

public class GatewayException extends Exception{

    @Serial
    private static final long serialVersionUID = 1L;

    public GatewayException() {}


    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }

}