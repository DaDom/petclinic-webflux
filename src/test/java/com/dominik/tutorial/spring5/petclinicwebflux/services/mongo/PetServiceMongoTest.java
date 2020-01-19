package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceMongoTest {

    @Mock
    private OwnerService ownerService;
    @InjectMocks
    private PetServiceMongo petService;

    @Test
    void testFindByIdExistingPet() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        String petName = "PET";
        List<Pet> petList = new ArrayList<>();
        petList.add(Pet.builder()
                .id(petId)
                .name(petName)
                .build());
        petList.add(Pet.builder()
                .id(UUID.randomUUID())
                .build());
        petList.add(Pet.builder()
                .id(UUID.randomUUID())
                .build());
        Owner owner = Owner.builder()
                .id(ownerId)
                .pets(petList)
                .build();
        when(this.ownerService.getById(eq(ownerId))).thenReturn(Mono.just(owner));

        // when
        Mono<Pet> result = this.petService.findById(ownerId, petId);

        // then
        assertTrue(result.hasElement().block());
        assertEquals(petName, result.block().getName());
    }

    @Test
    void testFindByIdNotExistingPet() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        String petName = "PET";
        List<Pet> petList = new ArrayList<>();
        petList.add(Pet.builder()
                .id(petId)
                .name(petName)
                .build());
        petList.add(Pet.builder()
                .id(UUID.randomUUID())
                .build());
        petList.add(Pet.builder()
                .id(UUID.randomUUID())
                .build());
        Owner owner = Owner.builder()
                .id(ownerId)
                .pets(petList)
                .build();
        when(this.ownerService.getById(eq(ownerId))).thenReturn(Mono.just(owner));

        // when
        Mono<Pet> result = this.petService.findById(ownerId, UUID.randomUUID());

        // then
        assertFalse(result.hasElement().block());
    }

    @Test
    void testFindByIdNotExistingOwner() {
        // given
        when(this.ownerService.getById(any())).thenReturn(Mono.empty());

        // when / then
        assertThrows(EntityNotFoundException.class, () -> {
            this.petService.findById(UUID.randomUUID(), UUID.randomUUID()).block();
        });
    }

    @Test
    void testSaveNewPet() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();

        Pet pet = Pet.builder()
                .id(petId)
                .build();

        List<Pet> petList = new ArrayList<>();
        petList.add(Pet.builder().id(UUID.randomUUID()).build());
        petList.add(Pet.builder().id(UUID.randomUUID()).build());
        petList.add(Pet.builder().id(UUID.randomUUID()).build());
        Owner owner = Owner.builder()
                .id(ownerId)
                .pets(petList)
                .build();
        when(this.ownerService.getById(eq(ownerId))).thenReturn(Mono.just(owner));
        when(this.ownerService.save(eq(owner))).thenReturn(Mono.just(owner));

        // when
        Mono<Pet> result = this.petService.save(ownerId, pet);

        // then
        assertTrue(result.hasElement().block());
        assertEquals(4, owner.getPets().size());
        assertTrue(owner.getPets().contains(pet));
        verify(this.ownerService, times(1)).save(eq(owner));
    }

    @Test
    void testUpdateExistingPet() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();

        Pet existingPet = Pet.builder()
                .id(petId)
                .name("Rufus")
                .petType("Cat")
                .birthDate(LocalDate.of(2012, 12, 19))
                .visits(List.of(new Visit(), new Visit()))
                .build();

        Pet petFormData = Pet.builder()
                .id(petId)
                .name("NewPetName")
                .petType("Dog")
                .birthDate(LocalDate.of(2015, 2, 9))
                .visits(new ArrayList<>())
                .build();

        List<Pet> petList = new ArrayList<>();
        petList.add(Pet.builder().id(UUID.randomUUID()).build());
        petList.add(Pet.builder().id(UUID.randomUUID()).build());
        petList.add(existingPet);
        Owner owner = Owner.builder()
                .id(ownerId)
                .pets(petList)
                .build();
        when(this.ownerService.getById(eq(ownerId))).thenReturn(Mono.just(owner));
        when(this.ownerService.save(eq(owner))).thenReturn(Mono.just(owner));

        // when
        Pet result = this.petService.save(ownerId, petFormData).block();
        Pet updatedPet = this.petService.findById(ownerId, petId).block();

        // then
        assertNotNull(result);
        assertEquals(3, owner.getPets().size());
        assertEquals("NewPetName", updatedPet.getName());
        assertEquals("Dog", updatedPet.getPetType());
        assertEquals(2, updatedPet.getVisits().size());
        assertEquals(2015, updatedPet.getBirthDate().getYear());
        verify(this.ownerService, times(1)).save(eq(owner));
    }

    @Test
    void testAddVisit() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        UUID visitId = UUID.randomUUID();

        Visit visit = Visit.builder()
                .id(visitId)
                .date(LocalDate.of(2011, 4, 16))
                .description("Doctor Visit")
                .build();

        Pet existingPet = Pet.builder()
                .id(petId)
                .name("Rufus")
                .petType("Cat")
                .birthDate(LocalDate.of(2012, 12, 19))
                .visits(new ArrayList<>())
                .build();

        Pet petFormData = Pet.builder()
                .id(petId)
                .name("Rufus")
                .petType("Cat")
                .birthDate(LocalDate.of(2012, 12, 19))
                .visits(List.of(visit))
                .build();

        List<Pet> petList = new ArrayList<>();
        petList.add(existingPet);
        Owner owner = Owner.builder()
                .id(ownerId)
                .pets(petList)
                .build();
        when(this.ownerService.getById(eq(ownerId))).thenReturn(Mono.just(owner));
        when(this.ownerService.save(eq(owner))).thenReturn(Mono.just(owner));

        // when
        Pet result = this.petService.save(ownerId, petFormData).block();
        Pet updatedPet = this.petService.findById(ownerId, petId).block();

        // then
        assertNotNull(result);
        assertEquals(1, owner.getPets().size());
        assertEquals("Rufus", updatedPet.getName());
        assertEquals("Cat", updatedPet.getPetType());
        assertEquals(1, updatedPet.getVisits().size());
        assertEquals("Doctor Visit", updatedPet.getVisits().get(0).getDescription());
        assertEquals(2012, updatedPet.getBirthDate().getYear());
        verify(this.ownerService, times(1)).save(eq(owner));
    }

    @Test
    void testDeleteExisting() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();

        Pet petToDelete = Pet.builder().id(petId).build();

        List<Pet> petList = new ArrayList<>();
        petList.add(Pet.builder().id(UUID.randomUUID()).build());
        petList.add(Pet.builder().id(UUID.randomUUID()).build());
        petList.add(petToDelete);
        Owner owner = Owner.builder()
                .id(ownerId)
                .pets(petList)
                .build();
        when(this.ownerService.getById(eq(ownerId))).thenReturn(Mono.just(owner));
        when(this.ownerService.save(eq(owner))).thenReturn(Mono.just(owner));

        // when
        this.petService.delete(ownerId, petId).block();

        // then
        assertEquals(2, owner.getPets().size());
        assertFalse(owner.getPets().contains(petToDelete));
        verify(this.ownerService, times(1)).save(eq(owner));
    }

    @Test
    void testDeleteNotExisting() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();

        Pet petToDelete = Pet.builder().id(petId).build();

        List<Pet> petList = new ArrayList<>();
        petList.add(Pet.builder().id(UUID.randomUUID()).build());
        petList.add(Pet.builder().id(UUID.randomUUID()).build());
        Owner owner = Owner.builder()
                .id(ownerId)
                .pets(petList)
                .build();
        when(this.ownerService.getById(eq(ownerId))).thenReturn(Mono.just(owner));
        when(this.ownerService.save(eq(owner))).thenReturn(Mono.just(owner));

        // when
        this.petService.delete(ownerId, petId).block();

        // then
        assertEquals(2, owner.getPets().size());
        assertFalse(owner.getPets().contains(petToDelete));
        verify(this.ownerService, times(1)).save(eq(owner));
    }

    @Test
    void testDeleteNotExistingOwner() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();

        Owner owner = Owner.builder()
                .id(ownerId)
                .build();
        when(this.ownerService.getById(eq(ownerId))).thenReturn(Mono.empty());

        // when
        assertThrows(EntityNotFoundException.class, () -> {
            this.petService.delete(ownerId, petId).block();
        });
    }
}