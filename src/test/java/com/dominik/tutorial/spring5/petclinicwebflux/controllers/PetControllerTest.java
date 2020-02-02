package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.InvalidParameterException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetTypeService;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@DisplayName("Pet Controller")
@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    private static final int NUM_OWNERS = 3;
    private static final int NUM_PETS = 3;

    private static final String PET_KEY = "pet";
    private static final String IS_NEW_KEY = "isNew";

    private static final String EXPECTED_VIEW_CREATE_OR_UPDATE_PET_FORM = "pets/createOrUpdatePetForm";
    private static final String EXPECTED_REDIRECT_OWNER_DETAILS = "redirect:/owners/";

    @Mock
    private PetService petService;
    @Mock
    private OwnerService ownerService;
    @Mock
    private PetTypeService petTypeService;
    @Mock
    private WebDataBinder dataBinder;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private Model model;
    @InjectMocks
    private PetController controller;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS);
    }

    @DisplayName("should show new pet form")
    @Test
    void testShowNewPetForm() {
        // when
        String returnedView = this.controller.showNewPetForm(this.model);

        // then
        then(this.model).should(times(1)).addAttribute(IS_NEW_KEY, true);
        then(this.model).should(times(1)).addAttribute(eq(PET_KEY), any());
        assertThat(EXPECTED_VIEW_CREATE_OR_UPDATE_PET_FORM).isEqualTo(returnedView);
    }

    @DisplayName("should create pet with valid input")
    @Test
    void testCreatePetValid() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        Pet pet = this.testDataFactory.getPet();
        given(this.ownerService.getById(any())).willReturn(Mono.just(owner));
        given(this.petService.save(owner.getId(), pet)).willReturn(Mono.just(pet));
        this.addDataBinderMock(false);
        ArgumentCaptor<Pet> captor = ArgumentCaptor.forClass(Pet.class);

        // when
        String returnedView = this.controller.createPet(owner.getId().toString(), pet, this.model).block();

        // then
        then(this.model).shouldHaveNoInteractions();
        then(this.ownerService).should(times(1)).getById(owner.getId());
        then(this.petService).should(times(1)).save(eq(owner.getId()), captor.capture());
        Pet capturedPet = captor.getValue();
        assertThat(pet).isEqualToIgnoringGivenFields(capturedPet, "ownerId");
        assertThat(EXPECTED_REDIRECT_OWNER_DETAILS + owner.getId().toString()).isEqualTo(returnedView);
    }

    @DisplayName("should throw exception when creating pet for invalid owner UUID")
    @Test
    void testCreatePetInvalidOwnerUUID() {
        // given
        String uuid = "123";
        this.addDataBinderMock(false);

        // when
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.createPet(uuid, this.testDataFactory.getPet(), this.model).block();
        });

        // then
        then(this.ownerService).shouldHaveNoInteractions();
        then(this.petService).shouldHaveNoInteractions();
    }

    @DisplayName("should throw exception when creating pet for non-existing owner")
    @Test
    void testCreatePetNonExistingOwner() {
        // given
        UUID uuid = UUID.randomUUID();
        this.addDataBinderMock(false);
        given(this.ownerService.getById(uuid)).willReturn(Mono.empty());

        // when
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.createPet(uuid.toString(), this.testDataFactory.getPet(), this.model).block();
        });

        // then
        then(this.ownerService).should(times(1)).getById(uuid);
        then(this.petService).shouldHaveNoInteractions();
    }

    @DisplayName("should not create pet with invalid input")
    @Test
    void testCreatePetInvalid() {
        // given
        Pet pet = this.testDataFactory.getPet();
        pet.setName("");
        this.addDataBinderMock(true);

        // when
        String returnedView = this.controller.createPet(UUID.randomUUID().toString(), pet, this.model).block();

        // then
        then(this.model).should(times(1)).addAttribute(IS_NEW_KEY, true);
        then(this.model).should(times(1)).addAttribute(eq(PET_KEY), any(Pet.class));
        then(this.ownerService).shouldHaveNoInteractions();
        then(this.petService).shouldHaveNoInteractions();
        assertThat(EXPECTED_VIEW_CREATE_OR_UPDATE_PET_FORM).isEqualTo(returnedView);
    }

    @DisplayName("should show edit pet form")
    @Test
    void testShowEditPetForm() {
        // when
        Owner owner = this.testDataFactory.getOwner();
        Pet pet = this.testDataFactory.getPet();
        given(this.ownerService.getById(owner.getId())).willReturn(Mono.just(owner));
        given(this.petService.findByIdAndOwner(pet.getId(), owner.getId())).willReturn(Mono.just(pet));
        ArgumentCaptor<Pet> captor = ArgumentCaptor.forClass(Pet.class);

        // when
        String returnedView = this.controller.showEditPetForm(owner.getId().toString(), pet.getId().toString(),
                this.model).block();

        // then
        then(this.model).should(times(1)).addAttribute(IS_NEW_KEY, false);
        then(this.model).should(times(1)).addAttribute(eq(PET_KEY), captor.capture());
        assertThat(pet).isEqualToComparingFieldByField(captor.getValue());
        assertThat(EXPECTED_VIEW_CREATE_OR_UPDATE_PET_FORM).isEqualTo(returnedView);
    }

    @DisplayName("should throw exception for invalid owner or pet uuid when showing update pet form")
    @Test
    void testShowEditPetFormInvalidUUIDs() {
        // when
        String validUUID = UUID.randomUUID().toString();
        String invalidUUID = "123";

        // when
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.showEditPetForm(invalidUUID, invalidUUID, this.model).block();
        });
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.showEditPetForm(validUUID, invalidUUID, this.model).block();
        });

        // then
        then(this.ownerService).shouldHaveNoInteractions();
        then(this.petService).shouldHaveNoInteractions();
    }

    @DisplayName("should throw exception when showing edit pet form for non-existing owner")
    @Test
    void testShowEditPetFormNonExistingOwner() {
        // when
        String uuid = UUID.randomUUID().toString();
        given(this.ownerService.getById(any())).willReturn(Mono.empty());

        // when
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.showEditPetForm(uuid, uuid, this.model).block();
        });

        // then
        then(this.ownerService).should(times(1)).getById(any());
        then(this.petService).shouldHaveNoInteractions();
    }

    @DisplayName("should throw exception when showing edit pet form for non-existing pet")
    @Test
    void testShowEditPetFormNonExistingPet() {
        // when
        String uuid = UUID.randomUUID().toString();
        Owner owner = this.testDataFactory.getOwner();
        given(this.ownerService.getById(any())).willReturn(Mono.just(owner));
        given(this.petService.findByIdAndOwner(any(), any())).willReturn(Mono.empty());

        // when
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.showEditPetForm(uuid, uuid, this.model).block();
        });

        // then
        then(this.ownerService).should(times(1)).getById(any());
        then(this.petService).should(times(1)).findByIdAndOwner(any(), any());
    }

    @DisplayName("should throw exception for invalid owner or pet uuid when updating pet")
    @Test
    void testEditPetInvalidUUIDs() {
        // when
        String validUUID = UUID.randomUUID().toString();
        String invalidUUID = "123";
        Pet pet = this.testDataFactory.getPet();

        // when
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.editPet(invalidUUID, invalidUUID, pet, this.model).block();
        });
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.editPet(validUUID, invalidUUID, pet, this.model).block();
        });

        // then
        then(this.ownerService).shouldHaveNoInteractions();
        then(this.petService).shouldHaveNoInteractions();
    }

    @DisplayName("should throw exception when editing pet for non-existing owner")
    @Test
    void testEditPetNonExistingOwner() {
        // when
        String uuid = UUID.randomUUID().toString();
        given(this.ownerService.getById(any())).willReturn(Mono.empty());
        Pet pet = this.testDataFactory.getPet();

        // when
        this.addDataBinderMock();
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.editPet(uuid, uuid, pet, this.model).block();
        });

        // then
        then(this.ownerService).should(times(1)).getById(any());
        then(this.petService).shouldHaveNoInteractions();
    }

    @DisplayName("should throw exception when editing non-existing pet")
    @Test
    void testEditPetNonExistingPet() {
        // when
        String uuid = UUID.randomUUID().toString();
        Owner owner = this.testDataFactory.getOwner();
        Pet pet = this.testDataFactory.getPet();
        given(this.ownerService.getById(any())).willReturn(Mono.just(owner));
        given(this.petService.findByIdAndOwner(any(), any())).willReturn(Mono.empty());

        // when
        this.addDataBinderMock();
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.editPet(uuid, uuid, pet, this.model).block();
        });

        // then
        then(this.ownerService).should(times(1)).getById(any());
        then(this.petService).should(times(1)).findByIdAndOwner(any(), any());
    }

    @DisplayName("should not update invalid pet")
    @Test
    void testEditPetInvalid() {
        // given
        Pet pet = this.testDataFactory.getPet();
        pet.setName("");
        String uuid = UUID.randomUUID().toString();
        ArgumentCaptor<Pet> captor = ArgumentCaptor.forClass(Pet.class);

        // when
        this.addDataBinderMock(true);
        String returnedView = this.controller.editPet(uuid, uuid, pet, this.model).block();

        // then
        then(this.ownerService).shouldHaveNoInteractions();
        then(this.petService).shouldHaveNoInteractions();
        then(this.model).should(times(1)).addAttribute(IS_NEW_KEY, false);
        then(this.model).should(times(1)).addAttribute(eq(PET_KEY), captor.capture());
        assertThat(pet).isEqualToComparingFieldByField(captor.getValue());
        assertThat(EXPECTED_VIEW_CREATE_OR_UPDATE_PET_FORM).isEqualTo(returnedView);
    }

    @DisplayName("should update valid pet")
    @Test
    void testEditPetValid() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        Pet pet = this.testDataFactory.getPet();
        ArgumentCaptor<Pet> captor = ArgumentCaptor.forClass(Pet.class);
        given(this.ownerService.getById(owner.getId())).willReturn(Mono.just(owner));
        given(this.petService.findByIdAndOwner(pet.getId(), owner.getId())).willReturn(Mono.just(pet));
        given(this.petService.save(eq(owner.getId()), any(Pet.class))).willReturn(Mono.just(pet));

        // when
        this.addDataBinderMock();
        String returnedView = this.controller.editPet(owner.getId().toString(), pet.getId().toString(), pet, this.model).block();

        // then
        then(this.ownerService).should(times(1)).getById(owner.getId());
        then(this.petService).should(times(1)).findByIdAndOwner(pet.getId(), owner.getId());
        then(this.petService).should(times(1)).save(eq(owner.getId()), captor.capture());
        Pet capturedPet = captor.getValue();
        assertThat(pet).isEqualToComparingFieldByField(capturedPet);
        assertThat(EXPECTED_REDIRECT_OWNER_DETAILS + owner.getId().toString()).isEqualTo(returnedView);
    }

    private void addDataBinderMock() {
        this.addDataBinderMock(false);
    }

    private void addDataBinderMock(boolean hasErrors) {
        given(this.dataBinder.getBindingResult()).willReturn(this.bindingResult);
        given(this.bindingResult.hasErrors()).willReturn(hasErrors);
        this.controller.disallowIdBinding(this.dataBinder);
    }
}