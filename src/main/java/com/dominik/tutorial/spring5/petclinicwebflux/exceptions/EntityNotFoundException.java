package com.dominik.tutorial.spring5.petclinicwebflux.exceptions;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public static EntityNotFoundException failedIdLookup(Class type, String id) {
        return new EntityNotFoundException(type.getName().toString() + " with ID " + id + " not found");
    }
}
