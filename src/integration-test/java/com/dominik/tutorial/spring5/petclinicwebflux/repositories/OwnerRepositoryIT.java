package com.dominik.tutorial.spring5.petclinicwebflux.repositories;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("IT: Owner Repository")
@DataMongoTest
class OwnerRepositoryIT {

    private final OwnerRepository ownerRepository;

    @Autowired
    public OwnerRepositoryIT(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @BeforeEach
    void setUp() {
        /*
        Not using the TestDataFactory here because it's easier like this to test different search cases.
        Maybe valuable to change at a later point in time, but should be acceptable for now.
         */

        if (this.ownerRepository.findAll().count().block() <= 0) {
            Owner owner1 = Owner.builder()
                    .id(UUID.randomUUID())
                    .firstName("Dominik")
                    .lastName("Picker")
                    .address("Address")
                    .city("Bielefeld")
                    .telephone("1233232")
                    .build();
            Owner owner2 = Owner.builder()
                    .id(UUID.randomUUID())
                    .firstName("Jens")
                    .lastName("Schmitt")
                    .address("Address")
                    .city("Berlin")
                    .telephone("1233232")
                    .build();
            Owner owner3 = Owner.builder()
                    .id(UUID.randomUUID())
                    .firstName("Hauke")
                    .lastName("Meier")
                    .address("Address")
                    .city("München")
                    .telephone("1233232")
                    .build();

            this.ownerRepository.saveAll(List.of(owner1, owner2, owner3)).count().block();
        }
    }

    @DisplayName("should show search results when there are some")
    @Test
    void testFindByLastNameWithResults() {
        // when: search with 1 match; case insensitive required
        Flux<Owner> result = this.ownerRepository.findByLastNameContainingIgnoreCase("ICKE");
        // then
        assertEquals(1, result.count().block());
        assertEquals("Picker", result.blockFirst().getLastName());

        // when: search with 2 matches; case insensitive required
        result = this.ownerRepository.findByLastNameContainingIgnoreCase("ER");
        // then
        assertEquals(2, result.count().block());

        // when: search full name; case insensitive not required
        result = this.ownerRepository.findByLastNameContainingIgnoreCase("Schmitt");
        // then
        assertEquals(1, result.count().block());
        assertEquals("Schmitt", result.blockFirst().getLastName());

        // when: matching all; case insensitive required
        result = this.ownerRepository.findByLastNameContainingIgnoreCase("I");
        // then
        assertEquals(3, result.count().block());

        // when: empty search
        result = this.ownerRepository.findByLastNameContainingIgnoreCase("");
        // then
        assertEquals(3, result.count().block());
    }

    @DisplayName("should not show search results when there are none")
    @Test
    void testFindByLastNameWithoutResults() {
        // when
        Flux<Owner> result = this.ownerRepository.findByLastNameContainingIgnoreCase("Nothing matches");
        // then
        assertFalse(result.hasElements().block());

        // when
        result = this.ownerRepository.findByLastNameContainingIgnoreCase(" ");
        // then
        assertFalse(result.hasElements().block());
    }
}