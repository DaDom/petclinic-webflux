package com.dominik.tutorial.spring5.petclinicwebflux.services;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PetService {

    Mono<Pet> findById(UUID ownerId, UUID petId);
    Mono<Pet> save(UUID ownerId, Pet pet);
    Mono<Void> delete(UUID ownerId, UUID petId);
}
