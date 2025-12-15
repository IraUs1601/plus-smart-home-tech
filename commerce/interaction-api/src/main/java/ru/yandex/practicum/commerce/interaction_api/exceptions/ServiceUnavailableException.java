package ru.yandex.practicum.commerce.interaction_api.exceptions;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}