package com.dominik.tutorial.spring5.petclinicwebflux.services.inmemory;

import com.dominik.tutorial.spring5.petclinicwebflux.services.PetTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pet Type Service")
class PetTypeServiceInMemoryTest {

    @DisplayName("should return all types on findAll")
    @Test
    void testFindAll() {
        // given
        PetTypeService petTypeService = new PetTypeServiceInMemory();

        // when
        Flux<String> allTypes = petTypeService.findAll();
        List<String> allTypesList = allTypes.collectList().block();

        // then
        assertThat(allTypesList).hasSize(5);
        assertThat(allTypesList).contains(PetTypeServiceInMemory.PET_TYPE_CAT);
        assertThat(allTypesList).contains(PetTypeServiceInMemory.PET_TYPE_DOG);
        assertThat(allTypesList).contains(PetTypeServiceInMemory.PET_TYPE_BIRD);
        assertThat(allTypesList).contains(PetTypeServiceInMemory.PET_TYPE_HORSE);
        assertThat(allTypesList).contains(PetTypeServiceInMemory.PET_TYPE_RABBIT);

    }
}