package ru.rsreu.exchange.exception;

public class InvalidOrderValuesException extends RuntimeException {
    public InvalidOrderValuesException(String message) {
        super(message);
    }
}
