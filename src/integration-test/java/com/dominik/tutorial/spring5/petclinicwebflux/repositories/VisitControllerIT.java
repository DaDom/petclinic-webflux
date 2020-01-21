package com.dominik.tutorial.spring5.petclinicwebflux.repositories;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class VisitControllerIT {

    private final WebTestClient webTestClient;
    private final PetService petService;
    private final OwnerService ownerService;
    private final VisitService visitService;

    @Autowired
    public VisitControllerIT(WebTestClient webTestClient, PetService petService, OwnerService ownerService, VisitService visitService) {
        this.webTestClient = webTestClient;
        this.petService = petService;
        this.ownerService = ownerService;
        this.visitService = visitService;
    }

    @Test
    void testCreateVisit() {
        // given
        UUID ownerId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        Pet pet = Pet.builder()
                .id(petId)
                .petType("Cat")
                .name("Doreen")
                .birthDate(LocalDate.of(2012, 12, 23))
                .build();
        List<Pet> petList = new ArrayList<>();
        petList.add(pet);
        Owner owner = Owner.builder()
                .id(ownerId)
                .firstName("Firstname")
                .lastName("Lastname")
                .telephone("Phone")
                .address("Address")
                .city("City")
                .pets(petList)
                .build();
        Visit newVisit = Visit.builder()
                .id(UUID.randomUUID())
                .description("Doctor visit")
                .date(LocalDate.now())
                .build();

        // when
        this.ownerService.save(owner).block();
        this.petService.save(owner.getId(), pet).block();
        this.webTestClient.post()
                .uri("/owners/" + ownerId.toString() + "/pets/" + petId.toString() + "/visits/new")
                .body(BodyInserters.fromFormData(this.visitToFormData(newVisit)))
                .exchange()
                .expectStatus().is3xxRedirection();
        Pet savedPet = this.petService.findByIdAndOwner(petId, ownerId).block();

        // then
        List<Visit> visitList = savedPet.getVisits();
        assertEquals(1, visitList.size());
        assertThat(newVisit).isEqualToIgnoringGivenFields(visitList.get(0), "id", "petId");
    }

    private MultiValueMap<String, String> visitToFormData(Visit visit) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (visit.getId() != null) {
            result.add("id", visit.getId().toString());
        }
        if (visit.getDate() != null) {
            result.add("date", visit.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (visit.getDescription() != null) {
            result.add("description", visit.getDescription());
        }

        return result;
    }
}
