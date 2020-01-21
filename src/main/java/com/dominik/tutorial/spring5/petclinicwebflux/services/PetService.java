package com.dominik.tutorial.spring5.petclinicwebflux.services;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PetService {

    Mono<Pet> findByIdAndOwner(UUID petId, UUID ownerId);
    Mono<Pet> findById(UUID petId);
    Flux<Pet> findByOwnerId(UUID ownerId);
    Mono<Pet> save(UUID ownerId, Pet pet);
    Mono<Void> delete(UUID petId);
}
