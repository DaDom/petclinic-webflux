package com.dominik.tutorial.spring5.petclinicwebflux.repositories;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Vet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VetRepository extends ReactiveMongoRepository<Vet, UUID> {
}
