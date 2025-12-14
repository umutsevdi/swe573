package com.hive.config.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class AccountSuspendedException extends BadCredentialsException {
    public AccountSuspendedException() {
        super("Account has been suspended.");
    }
}