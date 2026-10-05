package com.dekersan.franchise_api.infrastructure.entrypoint.web;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}