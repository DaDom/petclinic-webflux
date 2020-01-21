package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.PetRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PetServiceMongo implements PetService {

    private final PetRepository petRepository;
    private final VisitService visitService;

    public PetServiceMongo(PetRepository petRepository, VisitService visitService) {
        this.petRepository = petRepository;
        this.visitService = visitService;
    }

    @Override
    public Mono<Pet> findByIdAndOwner(UUID petId, UUID ownerId) {
        return this.petRepository.findByIdAndOwnerId(petId, ownerId)
                .flatMap(this::addVisits);
    }

    @Override
    public Mono<Pet> findById(UUID petId) {
        return this.petRepository.findById(petId)
                .flatMap(this::addVisits);
    }

    @Override
    public Flux<Pet> findByOwnerId(UUID ownerId) {
        return this.petRepository.findByOwnerId(ownerId)
                .flatMap(this::addVisits);
    }

    @Override
    public Mono<Pet> save(UUID ownerId, Pet pet) {
        pet.setOwnerId(ownerId);
        return this.petRepository.save(pet);
    }

    @Override
    public Mono<Void> delete(UUID petId) {
        return this.petRepository.deleteById(petId);
    }

    private Mono<Pet> addVisits(Pet pet) {
        return this.visitService.findByPet(pet.getId())
                .collectList()
                .flatMap(list -> {
                    pet.setVisits(list);
                    return Mono.just(pet);
                });
    }
}
