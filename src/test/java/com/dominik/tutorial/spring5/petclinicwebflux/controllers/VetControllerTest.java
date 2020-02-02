package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VetService;
import com.dominik.tutorial.spring5.petclinicwebflux.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@DisplayName("Vet Controller")
@ExtendWith(MockitoExtension.class)
class VetControllerTest {

    private static final int NUM_VETS = 3;
    private static final String MODEL_ATTRIBUTE_VETS = "vets";
    private static final String EXPECTED_VIEW_LIST_VETS = "vets/index";

    @Mock
    private VetService vetService;
    @Mock
    private Model model;
    @InjectMocks
    private VetController controller;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = TestDataFactory.vetsOnly(NUM_VETS);
    }

    @DisplayName("should display all vets")
    @Test
    void showAllVets() {
        // given
        given(this.vetService.findAll()).willReturn(Flux.fromIterable(this.testDataFactory.getVets()));
        ArgumentCaptor<Flux<Vet>> captor = ArgumentCaptor.forClass(Flux.class);

        // when
        String returnedView = this.controller.showAllVets(this.model);

        // then
        then(this.model).should(times(1)).addAttribute(eq(MODEL_ATTRIBUTE_VETS), captor.capture());
        List<Vet> capturedVets = captor.getValue().collectList().block();
        assertThat(capturedVets).hasSize(NUM_VETS);
        for (int i = 0; i < NUM_VETS; i++) {
            assertThat(this.testDataFactory.getVets().get(i)).isEqualToComparingFieldByField(capturedVets.get(i));
        }
        assertThat(EXPECTED_VIEW_LIST_VETS).isEqualTo(returnedView);
    }
}