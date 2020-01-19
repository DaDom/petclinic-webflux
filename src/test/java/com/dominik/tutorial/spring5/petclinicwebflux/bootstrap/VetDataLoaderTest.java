package com.dominik.tutorial.spring5.petclinicwebflux.bootstrap;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VetDataLoaderTest {

    @Mock
    private VetService vetService;
    @InjectMocks
    private VetDataLoader vetDataLoader;

    @Test
    void testRunOnEmpty() throws Exception {
        // given
        when(this.vetService.findAll()).thenReturn(Flux.empty());
        when(this.vetService.save(any())).thenReturn(Mono.just(new Vet()));

        // when
        this.vetDataLoader.run();

        // then
        verify(this.vetService, times(2)).save(any());
    }

    @Test
    void testRunOnNotEmpty() throws Exception {
        // given
        Vet vet = Vet.builder()
                .id(UUID.randomUUID())
                .firstName("Bryan")
                .lastName("Williams")
                .specialties(List.of("Dental", "Radiology"))
                .build();

        when(this.vetService.findAll()).thenReturn(Flux.just(vet));

        // when
        this.vetDataLoader.run();

        // then
        verify(this.vetService, never()).save(any());
    }

}