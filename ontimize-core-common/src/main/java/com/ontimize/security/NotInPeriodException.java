package com.ontimize.security;

/**
 * Custom Exception.
 *
 * @author Imatia Innovation
 */
public class NotInPeriodException extends GeneralSecurityException {

    public NotInPeriodException(String message) {
        super(message);
    }

}
