package com.thoughtworks.paranamer;

/**
 * Exception thrown when no parameter names are found
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class ParameterNamesNotFoundException extends RuntimeException {
    public ParameterNamesNotFoundException(String message) {
        super(message);
    }
}
