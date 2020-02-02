package com.dominik.tutorial.spring5.petclinicwebflux.bootstrap;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VetService;
import com.dominik.tutorial.spring5.petclinicwebflux.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@DisplayName("Vet Data Loader")
@ExtendWith(MockitoExtension.class)
class VetDataLoaderTest {

    @Mock
    private VetService vetService;
    @InjectMocks
    private VetDataLoader vetDataLoader;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = TestDataFactory.vetsOnly(1);
    }

    @DisplayName("should create new data on empty DB")
    @Test
    void testRunOnEmpty() throws Exception {
        // given
        given(this.vetService.findAll()).willReturn(Flux.empty());
        given(this.vetService.save(any(Vet.class))).willReturn(Mono.just(new Vet()));

        // when
        this.vetDataLoader.run();

        // then
        then(this.vetService).should(times(2)).save(any(Vet.class));
    }

    @DisplayName("should not create new data when already exists")
    @Test
    void testRunOnNotEmpty() throws Exception {
        // given
        given(this.vetService.findAll()).willReturn(Flux.just(this.testDataFactory.getVet()));

        // when
        this.vetDataLoader.run();

        // then
        then(this.vetService).shouldHaveNoMoreInteractions();
    }

}