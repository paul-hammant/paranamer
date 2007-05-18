package com.thoughtworks.paranamer;

/**
 * Exception thrown when no parameter names are found
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class ParameterNamesNotFoundException extends RuntimeException {

    static final String __PARANAMER_DATA = "v1.0 \n"
      + "<init> java.lang.String message \n";

    public ParameterNamesNotFoundException(String message) {
        super(message);
    }
}
