package com.dominik.tutorial.spring5.petclinicwebflux.exceptions;

public class InvalidParameterException extends RuntimeException {

    public InvalidParameterException(String message) {
        super(message);
    }

    public static InvalidParameterException invalidUUID(Class entityClass, String givenId) {
        return new InvalidParameterException("Given UUID '" + givenId + "' for entity " + entityClass.getSimpleName()
                + " is invalid");
    }
}
