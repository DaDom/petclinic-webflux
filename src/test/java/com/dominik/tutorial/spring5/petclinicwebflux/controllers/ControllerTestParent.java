package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import org.springframework.test.web.reactive.server.FluxExchangeResult;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class ControllerTestParent {

    protected void verifyView(String expectedView, FluxExchangeResult result) {
        assertTrue(new String(result.getResponseBodyContent()).contains("<!-- VIEW:" + expectedView + " -->"));
    }
}
