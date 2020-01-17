package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.InvalidParameterException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BaseControllerTest {

    @Test
    void testFromStringOrThrowValid() {
        // given
        BaseController controller = new BaseController();
        String id = UUID.randomUUID().toString();

        // when
        String convertedId = controller.fromStringOrThrow(id, BaseController.class).toString();

        // then
        assertEquals(id, convertedId);
    }

    @Test
    void testFromStringOrThrowInvalid() {
        // given
        String id = "123";
        String expectedMessage = "Given UUID '" + id + "' for entity BaseController is invalid";
        BaseController controller = new BaseController();

        // when
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            controller.fromStringOrThrow(id, BaseController.class);
        });

        // then
        assertEquals(expectedMessage, exception.getMessage());
    }
}