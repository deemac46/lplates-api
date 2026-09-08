package com.dmc.lplates.service;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}