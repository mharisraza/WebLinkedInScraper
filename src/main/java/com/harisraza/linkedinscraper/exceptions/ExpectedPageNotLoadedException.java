package com.harisraza.linkedinscraper.exceptions;

public class ExpectedPageNotLoadedException extends RuntimeException{
    public ExpectedPageNotLoadedException(String message) {
        super(message);
    }
}
