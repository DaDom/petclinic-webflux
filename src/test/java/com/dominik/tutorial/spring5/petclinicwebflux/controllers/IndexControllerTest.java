package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@DisplayName("Index Controller")
@ExtendWith(MockitoExtension.class)
class IndexControllerTest {

    private static final String WELCOME_MESSAGE_KEY = "welcome";
    private static final String EXPECTED_VIEW_INDEX = "index";

    @Mock
    private Model model;
    private IndexController controller;

    @BeforeEach
    void setUp() {
        this.controller = new IndexController();
    }

    @DisplayName("should show index")
    @Test
    void testStartPage() {
        // when
        String resultView = this.controller.startPage(this.model).block();

        // then
        then(this.model).should(times(1)).addAttribute(eq(WELCOME_MESSAGE_KEY), any());
        assertThat(EXPECTED_VIEW_INDEX).isEqualTo(resultView);
    }
}