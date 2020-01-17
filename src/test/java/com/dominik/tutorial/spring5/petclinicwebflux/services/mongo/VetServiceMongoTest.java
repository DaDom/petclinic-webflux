package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.VetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VetServiceMongoTest {

    @Mock
    private VetRepository vetRepository;
    @InjectMocks
    private VetServiceMongo vetServiceMongo;

    @Test
    void findAll() {
        // given
        Vet vet = Vet.builder()
                .id(UUID.randomUUID())
                .firstName("Dominik")
                .lastName("Picker")
                .build();
        when(this.vetRepository.findAll()).thenReturn(Flux.just(vet));

        // when
        Flux<Vet> result = this.vetServiceMongo.findAll();

        // then
        assertEquals(1, result.count().block());
        assertEquals("Dominik", result.blockFirst().getFirstName());
        verify(this.vetRepository, times(1)).findAll();
    }

    @Test
    void testSave() {
        // given
        Vet vet = Vet.builder()
                .id(UUID.randomUUID())
                .firstName("Dominik")
                .lastName("Picker")
                .build();
        when(this.vetRepository.save(any())).thenReturn(Mono.just(vet));

        // when
        Mono<Vet> result = this.vetServiceMongo.save(vet);

        // then
        assertTrue(result.hasElement().block());
        verify(this.vetRepository, times(1)).save(any());
        assertEquals("Dominik", result.block().getFirstName());
    }
}