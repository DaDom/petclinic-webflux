package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.PetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceMongoTest {

    @Mock
    private OwnerService ownerService;
    @Mock
    private VisitService visitService;
    @Mock
    private PetRepository petRepository;
    @InjectMocks
    private PetServiceMongo petService;

    @Test
    void testFindByIdExistingPet() {
        // given
        UUID petId = UUID.randomUUID();
        String petName = "PET";
        Pet targetPet = Pet.builder()
                .id(petId)
                .name(petName)
                .build();
        when(this.petRepository.findById(eq(petId))).thenReturn(Mono.just(targetPet));
        when(this.visitService.findByPet(eq(petId))).thenReturn(Flux.just(new Visit()));

        // when
        Mono<Pet> result = this.petService.findById(petId);

        // then
        Pet resultPet = result.block();
        assertNotNull(resultPet);
        assertEquals(petName, resultPet.getName());
        assertEquals(1, resultPet.getVisits().size());
    }

    @Test
    void testFindByOwnerIdExistingPet() {
        // given
        UUID petId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        String petName = "PET";
        Pet targetPet = Pet.builder()
                .id(petId)
                .name(petName)
                .build();
        when(this.petRepository.findByIdAndOwnerId(eq(petId), eq(ownerId))).thenReturn(Mono.just(targetPet));
        when(this.visitService.findByPet(eq(petId))).thenReturn(Flux.just(new Visit()));

        // when
        Mono<Pet> result = this.petService.findByIdAndOwner(petId, ownerId);

        // then
        Pet resultPet = result.block();
        assertNotNull(resultPet);
        assertEquals(petName, resultPet.getName());
        assertEquals(1, resultPet.getVisits().size());
    }

    @Test
    void testFindByOwnerIdNotExistingPet() {
        // given
        UUID petId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(this.petRepository.findByIdAndOwnerId(eq(petId), eq(ownerId))).thenReturn(Mono.empty());

        // when
        Mono<Pet> result = this.petService.findByIdAndOwner(petId, ownerId);

        // then
        assertFalse(result.hasElement().block());
    }

    @Test
    void testFindByIdNotExistingPet() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        when(this.petRepository.findById(eq(petId))).thenReturn(Mono.empty());

        // when
        Mono<Pet> result = this.petService.findById(petId);

        // then
        assertFalse(result.hasElement().block());
        verify(this.petRepository, times(1)).findById(eq(petId));
    }

    @Test
    void testSaveNewPet() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        Pet pet = Pet.builder()
                .id(petId)
                .build();
        when(this.petRepository.save(eq(pet))).thenReturn(Mono.just(pet));

        // when
        this.petService.save(ownerId, pet).block();

        // then
        verify(this.petRepository, times(1)).save(eq(pet));
    }

    @Test
    void testDelete() {
        // given
        UUID petId = UUID.randomUUID();
        when(this.petRepository.deleteById(eq(petId))).thenReturn(Mono.empty());

        // when
        this.petService.delete(petId).block();

        // then
        verify(this.petRepository, times(1)).deleteById(eq(petId));
    }
}