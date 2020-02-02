package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

@DisplayName("Vet Service Mongo")
@ExtendWith(MockitoExtension.class)
class VetServiceMongoTest {

    private static final int NUM_VETS = 2;

    @Mock
    private VetRepository vetRepository;
    @InjectMocks
    private VetServiceMongo vetServiceMongo;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = TestDataFactory.vetsOnly(NUM_VETS);
    }

    @DisplayName("should find all vets on findAll")
    @Test
    void findAll() {
        // given
        given(this.vetRepository.findAll()).willReturn(Flux.fromIterable(this.testDataFactory.getVets()));

        // when
        List<Vet> result = this.vetServiceMongo.findAll().collectList().block();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(NUM_VETS);
        for (int i = 0; i < NUM_VETS; i++) {
            assertThat(this.testDataFactory.getVets().get(i)).isEqualToComparingFieldByField(result.get(i));
        }
        then(this.vetRepository).should(times(1)).findAll();
    }

    @DisplayName("should save new vet to repository")
    @Test
    void testSave() {
        // given
        Vet vet = this.testDataFactory.getVet();
        given(this.vetRepository.save(any(Vet.class))).willReturn(Mono.just(vet));
        ArgumentCaptor<Vet> captor = ArgumentCaptor.forClass(Vet.class);

        // when
        Vet result = this.vetServiceMongo.save(vet).block();

        // then
        then(this.vetRepository).should(times(1)).save(captor.capture());
        assertThat(result).isNotNull();
        assertThat(vet).isEqualToComparingFieldByField(result);
        assertThat(vet).isEqualToComparingFieldByField(captor.getValue());
    }
}