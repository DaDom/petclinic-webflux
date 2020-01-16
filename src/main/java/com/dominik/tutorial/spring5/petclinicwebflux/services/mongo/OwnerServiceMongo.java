package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.OwnerRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class OwnerServiceMongo implements OwnerService {

    private final OwnerRepository ownerRepository;

    public OwnerServiceMongo(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public Flux<Owner> findAll() {
        return this.ownerRepository.findAll();
    }

    @Override
    public Mono<Owner> getById(UUID id) {
        return this.ownerRepository.findById(id);
    }

    @Override
    public Mono<Owner> save(Owner owner) {
        return this.ownerRepository.save(owner);
    }

}
