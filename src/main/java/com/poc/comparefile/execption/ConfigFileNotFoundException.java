package com.poc.comparefile.execption;

public class ConfigFileNotFoundException extends Exception {
    public ConfigFileNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
