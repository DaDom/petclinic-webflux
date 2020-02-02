package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.InvalidParameterException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@DisplayName("Visit Controller")
@ExtendWith(MockitoExtension.class)
class VisitControllerTest {

    private static final int NUM_OWNERS = 1;
    private static final int NUM_PETS = 1;
    private static final int NUM_VISITS = 3;
    private static final String MODEL_ATTRIBUTE_VISIT = "visit";
    private static final String EXPECTED_VIEW_CREATE_VISIT = "pets/createOrUpdateVisitForm";
    private static final String EXPECTED_REDIRECT_OWNER_DETAILS = "redirect:/owners/";

    @Mock
    private VisitService visitService;
    @Mock
    private OwnerService ownerService;
    @Mock
    private PetService petService;
    @Mock
    private WebDataBinder dataBinder;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private Model model;
    @InjectMocks
    private VisitController controller;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = new TestDataFactory(NUM_OWNERS, NUM_PETS, NUM_VISITS);
    }

    @DisplayName("should throw exception when showing form for invalid owner or pet uuid")
    @Test
    void testShowCreateVisitFormInvalidUUIDs() {
        // given
        String validUUID = UUID.randomUUID().toString();
        String invalidUUID = "123";

        // when
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.showCreateVisitForm(invalidUUID, validUUID, this.model).block();
        });
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.showCreateVisitForm(validUUID, invalidUUID, this.model).block();
        });

        // then
        then(this.ownerService).shouldHaveNoInteractions();
        then(this.petService).shouldHaveNoInteractions();
        then(this.visitService).shouldHaveNoInteractions();
    }

    @DisplayName("should throw exception when showing form for non-existing owner")
    @Test
    void testShowCreateVisitFormNonExistingOwner() {
        // given
        UUID uuid = UUID.randomUUID();
        given(this.ownerService.getById(uuid)).willReturn(Mono.empty());

        // when
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.showCreateVisitForm(uuid.toString(), uuid.toString(), this.model).block();
        });

        // then
        then(this.ownerService).should(times(1)).getById(uuid);
        then(this.petService).shouldHaveNoInteractions();
        then(this.visitService).shouldHaveNoInteractions();
    }

    @DisplayName("should throw exception when showing form for non-existing pet")
    @Test
    void testShowCreateVisitFormNonExistingPet() {
        // given
        UUID uuid = UUID.randomUUID();
        Owner owner = this.testDataFactory.getOwner();
        given(this.ownerService.getById(uuid)).willReturn(Mono.just(owner));
        given(this.petService.findByIdAndOwner(uuid, uuid)).willReturn(Mono.empty());

        // when
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.showCreateVisitForm(uuid.toString(), uuid.toString(), this.model).block();
        });

        // then
        then(this.ownerService).should(times(1)).getById(uuid);
        then(this.petService).should(times(1)).findByIdAndOwner(uuid, uuid);
        then(this.visitService).shouldHaveNoInteractions();
    }

    @DisplayName("should show create visit form")
    @Test
    void testShowCreateVisitForm() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        Pet pet = this.testDataFactory.getPet();
        given(this.ownerService.getById(owner.getId())).willReturn(Mono.just(owner));
        given(this.petService.findByIdAndOwner(pet.getId(), owner.getId())).willReturn(Mono.just(pet));

        // when
        String returnedView = this.controller.showCreateVisitForm(owner.getId().toString(), pet.getId().toString(),
                this.model).block();

        // then
        then(this.ownerService).should(times(1)).getById(owner.getId());
        then(this.petService).should(times(1)).findByIdAndOwner(pet.getId(), owner.getId());
        then(this.visitService).shouldHaveNoInteractions();
        then(this.model).should(times(1)).addAttribute(eq(MODEL_ATTRIBUTE_VISIT), any(Visit.class));
        assertThat(EXPECTED_VIEW_CREATE_VISIT).isEqualTo(returnedView);
    }

    @DisplayName("should throw exception when creating visit for invalid owner or pet uuid")
    @Test
    void testCreateVisitInvalidUUIDs() {
        // given
        Visit visit = this.testDataFactory.getVisit();
        String validUUID = UUID.randomUUID().toString();
        String invalidUUID = "123";

        // when
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.createVisit(invalidUUID, validUUID, visit, this.model).block();
        });
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.createVisit(validUUID, invalidUUID, visit, this.model).block();
        });

        // then
        then(this.ownerService).shouldHaveNoInteractions();
        then(this.petService).shouldHaveNoInteractions();
        then(this.visitService).shouldHaveNoInteractions();
    }

    @DisplayName("should throw exception when creating visit for non-existing owner")
    @Test
    void testCreateVisitNonExistingOwner() {
        // given
        UUID uuid = UUID.randomUUID();
        Visit visit = this.testDataFactory.getVisit();
        given(this.ownerService.getById(uuid)).willReturn(Mono.empty());

        // when
        this.addDataBinderMock();
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.createVisit(uuid.toString(), uuid.toString(), visit, this.model).block();
        });

        // then
        then(this.ownerService).should(times(1)).getById(uuid);
        then(this.petService).shouldHaveNoInteractions();
        then(this.visitService).shouldHaveNoInteractions();
    }

    @DisplayName("should throw exception when creating visit for non-existing pet")
    @Test
    void testCreateVisitNonExistingPet() {
        // given
        UUID uuid = UUID.randomUUID();
        Owner owner = this.testDataFactory.getOwner();
        Visit visit = this.testDataFactory.getVisit();
        given(this.ownerService.getById(uuid)).willReturn(Mono.just(owner));
        given(this.petService.findByIdAndOwner(uuid, uuid)).willReturn(Mono.empty());

        // when
        this.addDataBinderMock();
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.createVisit(uuid.toString(), uuid.toString(), visit, this.model).block();
        });

        // then
        then(this.ownerService).should(times(1)).getById(uuid);
        then(this.petService).should(times(1)).findByIdAndOwner(uuid, uuid);
        then(this.visitService).shouldHaveNoInteractions();
    }

    @DisplayName("should not create visit for invalid input")
    @Test
    void testCreateVisitInvalid() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        Pet pet = this.testDataFactory.getPet();
        Visit visit = this.testDataFactory.getVisit();
        visit.setDate(null);
        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);

        // when
        this.addDataBinderMock(true);
        String returnedView = this.controller.createVisit(owner.getId().toString(), pet.getId().toString(), visit,
                this.model).block();

        // then
        then(this.model).should(times(1)).addAttribute(eq(MODEL_ATTRIBUTE_VISIT), captor.capture());
        assertThat(visit).isEqualToComparingFieldByField(captor.getValue());
        assertThat(EXPECTED_VIEW_CREATE_VISIT).isEqualTo(returnedView);
    }

    @DisplayName("should create visit for valid input")
    @Test
    void testCreateVisitValid() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        Pet pet = this.testDataFactory.getPet();
        Visit visit = this.testDataFactory.getVisit();
        visit.setDate(null);
        given(this.ownerService.getById(owner.getId())).willReturn(Mono.just(owner));
        given(this.petService.findByIdAndOwner(pet.getId(), owner.getId())).willReturn(Mono.just(pet));
        given(this.visitService.createVisit(eq(pet.getId()), any(Visit.class))).willReturn(Mono.just(visit));
        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);

        // when
        this.addDataBinderMock();
        String returnedView = this.controller.createVisit(owner.getId().toString(), pet.getId().toString(), visit,
                this.model).block();

        // then
        then(this.visitService).should(times(1)).createVisit(eq(pet.getId()), captor.capture());
        assertThat(visit).isEqualToComparingFieldByField(captor.getValue());
        assertThat(EXPECTED_REDIRECT_OWNER_DETAILS + owner.getId().toString()).isEqualTo(returnedView);
    }

    @DisplayName("should get owner for model")
    @Test
    void testOwnerToModel() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        given(this.ownerService.getById(owner.getId())).willReturn(Mono.just(owner));

        // when
        Owner resultOwner = this.controller.addOwnerToModel(owner.getId().toString()).block();

        // then
        assertThat(owner).isEqualToComparingFieldByField(resultOwner);
    }

    @DisplayName("should get pet for model")
    @Test
    void testPetToModel() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        Pet pet = this.testDataFactory.getPet();
        given(this.petService.findByIdAndOwner(pet.getId(), owner.getId())).willReturn(Mono.just(pet));

        // when
        Pet resultPet = this.controller.addPetToModel(owner.getId().toString(), pet.getId().toString()).block();

        // then
        assertThat(pet).isEqualToComparingFieldByField(resultPet);
    }

    @DisplayName("should get visits for model")
    @Test
    void testVisitsToModel() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        Pet pet = this.testDataFactory.getPet();
        List<Visit> visits = this.testDataFactory.getVisits();
        given(this.visitService.findByPet(pet.getId())).willReturn(Flux.fromIterable(visits));

        // when
        List<Visit> resultVisits = this.controller.addPetVisitsToModel(pet.getId().toString()).collectList().block();

        // then
        assertThat(resultVisits).hasSize(visits.size());
        for (int i = 0; i < visits.size(); i++) {
            assertThat(visits.get(i)).isEqualToComparingFieldByField(resultVisits.get(i));
        }
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