package com.barber_manager.auth_service.exceptions;

public class InvalidRegistrationException extends RuntimeException {

    public InvalidRegistrationException() {
        super("Only barber and administrator accounts can be created.");
    }
}
