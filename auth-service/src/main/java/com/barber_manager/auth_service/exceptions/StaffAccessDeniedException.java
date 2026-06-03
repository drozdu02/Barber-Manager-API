package com.barber_manager.auth_service.exceptions;

public class StaffAccessDeniedException extends RuntimeException {

    public StaffAccessDeniedException() {
        super("Login is restricted to barber and administrator accounts.");
    }
}
