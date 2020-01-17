package com.dominik.tutorial.spring5.petclinicwebflux.repositories;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface OwnerRepository extends ReactiveMongoRepository<Owner, UUID> {

    Flux<Owner> findByLastNameContainingIgnoreCase(String lastNameFragment);
}
