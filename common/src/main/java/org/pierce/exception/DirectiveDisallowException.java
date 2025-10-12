package org.pierce.exception;

public class DirectiveDisallowException extends RuntimeException {
    public DirectiveDisallowException(String message) {
        super(message);
    }
}
