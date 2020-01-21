package com.dominik.tutorial.spring5.petclinicwebflux.repositories;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PetRepository extends ReactiveMongoRepository<Pet, UUID> {

    Flux<Pet> findByOwnerId(UUID ownerId);
    Mono<Pet> findByIdAndOwnerId(UUID id, UUID ownerId);
}
