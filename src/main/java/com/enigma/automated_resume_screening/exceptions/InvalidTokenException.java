package com.enigma.automated_resume_screening.exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String msg) {
        super(msg);
    }
}
