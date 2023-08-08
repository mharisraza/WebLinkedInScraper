package com.harisraza.linkedinscraper.exceptions;

public class ResourceNotFound extends RuntimeException {
    private String resourceName;
    private String fieldValue;

    public ResourceNotFound(String resourceName, String fieldValue) {
        super(String.format("%s not found with %s", resourceName, fieldValue));
        this.resourceName = resourceName;
        this.fieldValue = fieldValue;
    }
}
