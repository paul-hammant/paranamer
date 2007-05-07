package com.thoughtworks.paranamer;

/**
 * Based expection for all checked Paranamer exceptions
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class ParanamerException extends Exception {
    public ParanamerException(String message) {
        super(message);
    }
}
