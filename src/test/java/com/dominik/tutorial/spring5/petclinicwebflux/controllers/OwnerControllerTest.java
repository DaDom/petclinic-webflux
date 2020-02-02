package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.InvalidParameterException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
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

@DisplayName("Owner Controller")
@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    private static final String EXPECTED_VIEW_SEARCH_FORM_REDIRECT = "redirect:/owners/find";
    private static final String EXPECTED_VIEW_OWNERS_LIST = "owners/ownersList";
    private static final String EXPECTED_VIEW_FIND_OWNER_FORM = "owners/findOwners";
    private static final String EXPECTED_VIEW_SHOW_OWNER_DETAILS = "owners/ownerDetails";
    private static final String EXPECTED_VIEW_CREATE_OR_UPDATE_OWNER_FORM = "owners/createOrUpdateOwnerForm";
    private static final String EXPECTED_VIEW_OWNER_CREATED = "redirect:/owners/";
    private static final String OWNER_SEARCH_RESULT_KEY = "selections";
    private static final String OWNER_KEY = "owner";
    private static final String IS_NEW_KEY = "isNew";
    private static final int NUM_OWNERS = 3;

    @Mock
    private Model model;
    @Mock
    private OwnerService ownerService;
    @Mock
    private WebDataBinder dataBinder;
    @Mock
    BindingResult bindingResult;
    @InjectMocks
    private OwnerController controller;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        this.testDataFactory = new TestDataFactory(NUM_OWNERS);
    }

    @DisplayName("should show form when no search")
    @Test
    void testFindOwnersNoSearchString() {
        // when
        String resultView = this.controller.findOwners(null, this.model);

        // then
        then(this.model).shouldHaveNoInteractions();
        assertThat(EXPECTED_VIEW_SEARCH_FORM_REDIRECT).isEqualTo(resultView);
    }

    @DisplayName("should show all when empty search string")
    @Test
    void testFindOwnersEmptySearchString() {
        // given
        given(this.ownerService.findAll()).willReturn(Flux.fromIterable(this.testDataFactory.getOwners()));
        ArgumentCaptor<Flux<Owner>> captor = ArgumentCaptor.forClass(Flux.class);

        // when
        String resultView = this.controller.findOwners("  ", this.model);

        // then
        then(this.ownerService).should(times(1)).findAll();
        then(this.model).should(times(1)).addAttribute(eq(OWNER_SEARCH_RESULT_KEY), captor.capture());
        List<Owner> ownerList = captor.getValue().collectList().block();
        assertThat(ownerList).isNotNull();
        for (int i = 0; i < NUM_OWNERS; i++) {
            assertThat(this.testDataFactory.getOwners().get(i)).isEqualToComparingFieldByField(ownerList.get(i));
        }
        assertThat(EXPECTED_VIEW_OWNERS_LIST).isEqualTo(resultView);
    }

    @DisplayName("should search by name with non-empty search string")
    @Test
    void testFindOwnersNonEmptySearchString() {
        // given
        String searchString = "anything";
        given(this.ownerService.findByLastNameFragment(searchString)).willReturn(Flux.fromIterable(this.testDataFactory.getOwners()));
        ArgumentCaptor<Flux<Owner>> captor = ArgumentCaptor.forClass(Flux.class);

        // when
        String resultView = this.controller.findOwners(searchString, this.model);

        // then
        then(this.ownerService).should(times(1)).findByLastNameFragment(eq(searchString));
        then(this.model).should(times(1)).addAttribute(eq(OWNER_SEARCH_RESULT_KEY), captor.capture());
        List<Owner> ownerList = captor.getValue().collectList().block();
        assertThat(ownerList).isNotNull();
        for (int i = 0; i < NUM_OWNERS; i++) {
            assertThat(this.testDataFactory.getOwners().get(i)).isEqualToComparingFieldByField(ownerList.get(i));
        }
        assertThat(EXPECTED_VIEW_OWNERS_LIST).isEqualTo(resultView);
    }

    @DisplayName("should show find owner form")
    @Test
    void testShowFindOwnerForm() {
        // when
        String resultView = this.controller.showFindOwnerForm(this.model);

        // then
        then(this.model).should(times(1)).addAttribute(eq(OWNER_KEY), any(Mono.class));
        assertThat(EXPECTED_VIEW_FIND_OWNER_FORM).isEqualTo(resultView);
    }

    @DisplayName("should show add owner form")
    @Test
    void testShowAddOwnerForm() {
        // when
        String resultView = this.controller.showAddOwnerForm(this.model);

        // then
        then(this.model).should(times(1)).addAttribute(eq(OWNER_KEY), any(Mono.class));
        then(this.model).should(times(1)).addAttribute(IS_NEW_KEY, true);
        assertThat(EXPECTED_VIEW_CREATE_OR_UPDATE_OWNER_FORM).isEqualTo(resultView);
    }

    @DisplayName("should create owner with valid input")
    @Test
    void testCreateOwnerValidInput() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        given(this.ownerService.save(any(Owner.class))).willReturn(Mono.just(owner));
        ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);

        // when
        this.controller.avoidIdFieldProcessing(new WebDataBinder(owner));
        String resultView = this.controller.createOwner(Mono.just(owner), this.model).block();

        // then
        then(this.ownerService).should(times(1)).save(captor.capture());
        assertThat(owner).isEqualToComparingFieldByField(captor.getValue());
        assertThat(EXPECTED_VIEW_OWNER_CREATED + owner.getId().toString()).isEqualTo(resultView);
    }

    @DisplayName("should create owner with valid input")
    @Test
    void testCreateOwnerInvalidInput() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        owner.setFirstName("");
        given(dataBinder.getBindingResult()).willReturn(bindingResult);
        given(bindingResult.hasErrors()).willReturn(true);
        this.controller.avoidIdFieldProcessing(this.dataBinder);

        // when
        String resultView = this.controller.createOwner(Mono.just(owner), this.model).block();

        // then
        then(this.model).should(times(1)).addAttribute(IS_NEW_KEY, true);
        assertThat(EXPECTED_VIEW_CREATE_OR_UPDATE_OWNER_FORM).isEqualTo(resultView);
    }

    @DisplayName("should show details of existing owner")
    @Test
    void testShowExistingOwnerDetails() {
        // given
        UUID uuid = UUID.randomUUID();
        given(this.ownerService.getById(any())).willReturn(Mono.just(this.testDataFactory.getOwner()));
        ArgumentCaptor<Mono> captor = ArgumentCaptor.forClass(Mono.class);

        // when
        String returnedView = this.controller.showOwnerDetails(uuid.toString(), this.model).block();

        // then
        then(this.ownerService).should(times(1)).getById(eq(uuid));
        then(this.model).should(times(1)).addAttribute(eq(OWNER_KEY), captor.capture());
        assertThat(this.testDataFactory.getOwner()).isEqualToComparingFieldByField(captor.getValue().block());
        assertThat(EXPECTED_VIEW_SHOW_OWNER_DETAILS).isEqualTo(returnedView);
    }

    @DisplayName("should throw exception for invalid UUID when showing owner details")
    @Test
    void testShowOwnerDetailsInvalidUUID() {
        // given
        String uuid = "123";

        // when
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.showOwnerDetails(uuid, this.model).block();
        });
    }

    @DisplayName("should throw exception when showing non-existing owner details")
    @Test
    void testShowNonExistingOwnerDetails() {
        // given
        UUID uuid = UUID.randomUUID();
        given(this.ownerService.getById(any())).willReturn(Mono.empty());

        // when
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.showOwnerDetails(uuid.toString(), this.model).block();
        });
        then(this.ownerService).should(times(1)).getById(eq(uuid));
    }

    @DisplayName("should show update owner form for existing owner")
    @Test
    void testShowUpdateExistingOwnerForm() {
        // given
        UUID uuid = UUID.randomUUID();
        given(this.ownerService.getById(any())).willReturn(Mono.just(this.testDataFactory.getOwner()));
        ArgumentCaptor<Mono> captor = ArgumentCaptor.forClass(Mono.class);

        // when
        String returnedView = this.controller.showUpdateOwnerForm(uuid.toString(), this.model).block();

        //then
        then(this.ownerService).should(times(1)).getById(eq(uuid));
        then(this.model).should(times(1)).addAttribute(eq(OWNER_KEY), captor.capture());
        then(this.model).should(times(1)).addAttribute(IS_NEW_KEY, false);
        assertThat(this.testDataFactory.getOwner()).isEqualToComparingFieldByField(captor.getValue().block());
        assertThat(EXPECTED_VIEW_CREATE_OR_UPDATE_OWNER_FORM).isEqualTo(returnedView);
    }

    @DisplayName("should throw exception when showing update owner form with invalid UUID")
    @Test
    void testShowUpdateOwnerFormInvalidUUID() {
        // given
        String uuid = "123";

        // when
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.showUpdateOwnerForm(uuid, this.model).block();
        });
    }

    @DisplayName("should throw exception when showing update non-existing owner form")
    @Test
    void testShowUpdateNonExistingOwnerForm() {
        // given
        UUID uuid = UUID.randomUUID();
        given(this.ownerService.getById(any())).willReturn(Mono.empty());

        // when
        assertThrows(EntityNotFoundException.class, () -> {
            this.controller.showUpdateOwnerForm(uuid.toString(), this.model).block();
        });
        then(this.ownerService).should(times(1)).getById(eq(uuid));
    }

    @DisplayName("should update owner with valid input")
    @Test
    void testUpdateOwnerValid() {
        // given
        Owner databaseOwner = this.testDataFactory.getOwner();
        Owner updatedOwner = this.testDataFactory.getOwners().get(1);
        given(this.ownerService.save(any())).willReturn(Mono.just(updatedOwner));
        ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);

        // when
        this.controller.avoidIdFieldProcessing(new WebDataBinder(updatedOwner));
        String returnedView = this.controller.updateOwner(databaseOwner.getId().toString(), Mono.just(updatedOwner),
                this.model).block();

        // then
        then(this.ownerService).should(times(1)).save(captor.capture());
        Owner capturedOwner = captor.getValue();
        assertThat(updatedOwner).isEqualToIgnoringGivenFields(capturedOwner, "id");
        assertThat(databaseOwner.getId()).isEqualTo(capturedOwner.getId());
        assertThat(EXPECTED_VIEW_OWNER_CREATED + databaseOwner.getId().toString()).isEqualTo(returnedView);
    }

    @DisplayName("should throw exception when updating owner with invalid UUID")
    @Test
    void testUpdateOwnerInvalidUUID() {
        // given
        String uuid = "123";

        // when
        assertThrows(InvalidParameterException.class, () -> {
            this.controller.updateOwner(uuid, Mono.just(this.testDataFactory.getOwner()), this.model).block();
        });
    }

    @DisplayName("should not update owner with invalid input")
    @Test
    void testUpdateOwnerInvalid() {
        // given
        Owner owner = this.testDataFactory.getOwner();
        owner.setFirstName("");
        given(this.dataBinder.getBindingResult()).willReturn(this.bindingResult);
        given(this.bindingResult.hasErrors()).willReturn(true);

        // when
        this.controller.avoidIdFieldProcessing(this.dataBinder);
        String returnedView = this.controller.updateOwner(owner.getId().toString(), Mono.just(owner), this.model).block();

        // then
        then(this.model).should(times(1)).addAttribute(IS_NEW_KEY, false);
        assertThat(EXPECTED_VIEW_CREATE_OR_UPDATE_OWNER_FORM).isEqualTo(returnedView);
    }
}