package com.hive.config;

import org.springframework.security.authentication.BadCredentialsException;

public class AuthenticationFailureException extends BadCredentialsException {
    public AuthenticationFailureException() {
        super("Bad credentials.");
    }
}
