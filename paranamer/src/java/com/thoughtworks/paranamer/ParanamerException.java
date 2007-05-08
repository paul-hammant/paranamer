package com.thoughtworks.paranamer;

/**
 * Base expection for all Paranamer exceptions
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class ParanamerException extends RuntimeException {
    public ParanamerException(String message) {
        super(message);
    }
}
