package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.InvalidParameterException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

public class BaseController {

    private static final String MODEL_ATTRIBUTE_ERROR = "error";
    private static final String VIEW_400_ERROR = "400error";

    protected UUID fromStringOrThrow(String givenId, Class forEntity) throws InvalidParameterException {
        try {
            return UUID.fromString(givenId);
        }
        catch (IllegalArgumentException e) {
            throw InvalidParameterException.invalidUUID(forEntity, givenId);
        }
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFound(EntityNotFoundException exception, Model model) {
        model.addAttribute(MODEL_ATTRIBUTE_ERROR, exception);
        return VIEW_400_ERROR;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidParameter(InvalidParameterException exception, Model model) {
        model.addAttribute(MODEL_ATTRIBUTE_ERROR, exception);
        return VIEW_400_ERROR;
    }
}
