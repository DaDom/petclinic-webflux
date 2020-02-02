package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.InvalidParameterException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@DisplayName("Base Controller")
@ExtendWith(MockitoExtension.class)
class BaseControllerTest {

    private final String MODEL_ATTRIBUTE_ERROR = "error";
    private final String EXPECTED_VIEW_ERROR = "400error";

    @Mock
    private Model model;
    private BaseController controller;

    @BeforeEach
    void setUp() {
        this.controller = new BaseController();
    }

    @DisplayName("should convert valid UUID")
    @Test
    void testFromStringOrThrowValid() {
        // given
        String id = UUID.randomUUID().toString();

        // when
        String convertedId = this.controller.fromStringOrThrow(id, BaseController.class).toString();

        // then
        assertThat(id).isEqualTo(convertedId);
    }

    @DisplayName("should throw Exception for invalid UUID")
    @Test
    void testFromStringOrThrowInvalid() {
        // given
        String id = "123";
        String expectedMessage = "Given UUID '" + id + "' for entity BaseController is invalid";

        // when
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            this.controller.fromStringOrThrow(id, BaseController.class);
        });

        // then
        assertThat(expectedMessage).isEqualTo(exception.getMessage());
    }

    @DisplayName("should add exceptions to model")
    @Test
    void testExceptionHandling() {
        // given
        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        EntityNotFoundException e1 = EntityNotFoundException.failedIdLookup(Owner.class, "123");
        InvalidParameterException e2 = InvalidParameterException.invalidUUID(Owner.class, "123");

        // when
        String returnedView1 = this.controller.handleEntityNotFound(e1, this.model);
        String returnedView2 = this.controller.handleInvalidParameter(e2, this.model);

        // then
        then(this.model).should(times(1)).addAttribute(eq(MODEL_ATTRIBUTE_ERROR), eq(e1));
        then(this.model).should(times(1)).addAttribute(eq(MODEL_ATTRIBUTE_ERROR), eq(e2));
        assertThat(EXPECTED_VIEW_ERROR).isEqualTo(returnedView1);
        assertThat(EXPECTED_VIEW_ERROR).isEqualTo(returnedView2);
    }
}