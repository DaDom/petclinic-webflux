package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.OwnerRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class OwnerServiceMongo implements OwnerService {

    private final OwnerRepository ownerRepository;
    private final PetService petService;

    public OwnerServiceMongo(OwnerRepository ownerRepository, PetService petService) {
        this.ownerRepository = ownerRepository;
        this.petService = petService;
    }

    @Override
    public Flux<Owner> findAll() {
        return this.ownerRepository.findAll()
                .flatMap(this::addPets);
    }

    @Override
    public Mono<Owner> getById(UUID id) {
        return this.ownerRepository.findById(id)
                .flatMap(this::addPets);
    }

    @Override
    public Mono<Owner> save(Owner owner) {
        return this.ownerRepository.save(owner);
    }

    @Override
    public Flux<Owner> findByLastNameFragment(String lastNameFragment) {
        return this.ownerRepository.findByLastNameContainingIgnoreCase(lastNameFragment)
                .flatMap(this::addPets);

    }

    private Mono<Owner> addPets(Owner owner) {
        return this.petService.findByOwnerId(owner.getId())
                .collectList()
                .flatMap(list -> {
                    owner.setPets(list);
                    return Mono.just(owner);
                });
    }

}
