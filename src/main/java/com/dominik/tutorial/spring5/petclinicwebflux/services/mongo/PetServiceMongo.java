package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class PetServiceMongo implements PetService {

    private final OwnerService ownerService;

    public PetServiceMongo(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @Override
    public Mono<Pet> findById(UUID ownerId, UUID petId) {
        return this.ownerService.getById(ownerId)
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId.toString())))
                .flatMap(o -> this.getPetFromOwner(o, petId));
    }

    @Override
    public Mono<Pet> save(UUID ownerId, Pet pet) {
        return this.ownerService.getById(ownerId)
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId.toString())))
                .flatMap(o -> this.removePetFromOwner(o, pet.getId()))
                .flatMap(o -> this.addPetToOwnerAndSave(o, pet));
    }

    @Override
    public Mono<Void> delete(UUID ownerId, UUID petId) {
        return this.ownerService.getById(ownerId)
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId.toString())))
                .flatMap(o -> this.removePetFromOwner(o, petId))
                .flatMap(this.ownerService::save)
                .flatMap(o -> Mono.empty());
    }

    private Mono<Pet> addPetToOwnerAndSave(Owner owner, Pet pet) {
        owner.getPets().add(pet);
        return this.ownerService.save(owner)
                .flatMap(o -> this.getPetFromOwner(o, pet.getId()));
    }

    private Mono<Pet> getPetFromOwner(Owner o, UUID petId) {
        for (Pet pet : o.getPets()) {
            if (pet != null && pet.getId() != null && pet.getId().toString().equals(petId.toString())) {
                return Mono.just(pet);
            }
        }
        return Mono.empty();
    }

    private Mono<Owner> removePetFromOwner(Owner o, UUID petId) {
        List<Pet> newPetList = new ArrayList<>();
        for (Pet pet : o.getPets()) {
            if (pet != null && pet.getId() != null && !pet.getId().equals(petId)) {
                newPetList.add(pet);
            }
        }
        o.setPets(newPetList);
        return Mono.just(o);
    }
}
