package com.abatye.family_help_uae.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an operation is not permitted given the current state of a resource.
 * Examples: editing a non-OPEN request, deleting an ACTIVE task, self-review.
 * Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class Sec103_1093910_InvalidOperationException extends RuntimeException {

    public Sec103_1093910_InvalidOperationException(String message) {
        super(message);
    }
}
