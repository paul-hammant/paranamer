package com.thoughtworks.paranamer;

/**
 * Based expection for all runtime unchecked Paranamer exceptions
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class ParanamerRuntimeException extends RuntimeException {
    public ParanamerRuntimeException(String message) {
        super(message);
    }
}
