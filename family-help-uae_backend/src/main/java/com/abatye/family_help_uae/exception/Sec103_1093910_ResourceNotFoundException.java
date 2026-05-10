package com.abatye.family_help_uae.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested entity does not exist in the database.
 * Maps to HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class Sec103_1093910_ResourceNotFoundException extends RuntimeException {

    public Sec103_1093910_ResourceNotFoundException(String message) {
        super(message);
    }
}
