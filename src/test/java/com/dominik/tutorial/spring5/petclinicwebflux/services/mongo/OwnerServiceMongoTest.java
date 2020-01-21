package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.OwnerRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerServiceMongoTest {

    @Mock
    private OwnerRepository ownerRepository;
    @Mock
    private PetService petService;
    @InjectMocks
    private OwnerServiceMongo ownerServiceMongo;

    @Test
    void testFindAll() {
        // given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Owner owner1 = Owner.builder()
                .id(id1)
                .firstName("Dominik")
                .lastName("Picker")
                .build();
        Owner owner2 = Owner.builder()
                .id(id2)
                .firstName("Dennis")
                .lastName("Huchzermeyer")
                .build();
        when(this.ownerRepository.findAll()).thenReturn(Flux.just(owner1, owner2));
        when(this.petService.findByOwnerId(any())).thenReturn(Flux.just(new Pet()));

        // when
        Flux<Owner> result = this.ownerServiceMongo.findAll();

        // then
        assertNotNull(result);
        List<Owner> resultList = result.collectList().block();
        assertEquals(2, resultList.size());
        assertThat(owner1).isEqualToIgnoringGivenFields(resultList.get(0), "pets");
        assertThat(owner2).isEqualToIgnoringGivenFields(resultList.get(1), "pets");
        assertEquals(1, resultList.get(0).getPets().size());
        assertEquals(1, resultList.get(1).getPets().size());
        verify(this.ownerRepository, times(1)).findAll();
    }

    @Test
    void testFindAllEmpty() {
        // given
        when(this.ownerRepository.findAll()).thenReturn(Flux.empty());

        // when
        Flux<Owner> result = this.ownerServiceMongo.findAll();

        // then
        assertNotNull(result);
        assertFalse(result.hasElements().block());
    }

    @Test
    void testGetById() {
        // given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Owner owner = Owner.builder()
                .id(id1)
                .firstName("Dominik")
                .lastName("Picker")
                .build();
        when(this.ownerRepository.findById(eq(id1))).thenReturn(Mono.just(owner));
        when(this.ownerRepository.findById(not(eq(id1)))).thenReturn(Mono.empty());
        when(this.petService.findByOwnerId(any())).thenReturn(Flux.empty());

        // when
        Mono<Owner> resultMonoExists = this.ownerServiceMongo.getById(id1);
        Mono<Owner> resultMonoNotExists = this.ownerServiceMongo.getById(id2);

        // then
        assertFalse(resultMonoNotExists.hasElement().block());
        assertTrue(resultMonoExists.hasElement().block());
        assertEquals(id1, resultMonoExists.block().getId());
        assertEquals("Dominik", resultMonoExists.block().getFirstName());
        assertEquals("Picker", resultMonoExists.block().getLastName());
        verify(this.ownerRepository, times(1)).findById(id1);
        verify(this.ownerRepository, times(1)).findById(id2);
    }

    @Test
    void testSave() {
        // given
        UUID ownerId = UUID.randomUUID();
        Owner owner = Owner.builder()
                .id(ownerId)
                .firstName("Dominik")
                .lastName("Picker")
                .address("Address")
                .city("City")
                .telephone("123123")
                .pets(new ArrayList<>())
                .build();
        when(this.ownerRepository.save(any())).thenReturn(Mono.just(owner));

        // when
        Owner result = this.ownerServiceMongo.save(owner).block();

        // then
        assertNotNull(result);
        assertEquals(owner, result);
        verify(this.ownerRepository, times(1)).save(any());
    }

    @Test
    void testFindByLastNameFragment() {
        // given
        Owner owner = Owner.builder()
                .id(null)
                .firstName("Dominik")
                .lastName("Picker")
                .address("Address")
                .city("City")
                .telephone("123123")
                .pets(new ArrayList<>())
                .build();
        when(this.ownerRepository.findByLastNameContainingIgnoreCase(anyString())).thenReturn(Flux.just(owner));
        when(this.petService.findByOwnerId(any())).thenReturn(Flux.just(new Pet()));

        // when
        Flux<Owner> result = this.ownerServiceMongo.findByLastNameFragment("anything");

        // then
        Owner resultOwner = result.blockFirst();
        assertNotNull(resultOwner);
        assertEquals(owner, resultOwner);
        assertEquals(1, resultOwner.getPets().size());
        assertThat(owner).isEqualToIgnoringGivenFields(resultOwner, "id", "pets");
        verify(this.ownerRepository, times(1)).findByLastNameContainingIgnoreCase(anyString());
    }
}