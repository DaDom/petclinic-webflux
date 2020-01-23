package com.dominik.tutorial.spring5.petclinicwebflux.services.mongo;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.repositories.OwnerRepository;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
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
                .flatMap(o -> this.addPets(Mono.just(o), o.getId()));
    }

    @Override
    public Mono<Owner> getById(UUID id) {
        return this.ownerRepository.findById(id)
                .flatMap(o -> this.addPets(Mono.just(o), id));
    }

    @Override
    public Mono<Owner> save(Owner owner) {
        return this.ownerRepository.save(owner);
    }

    @Override
    public Flux<Owner> findByLastNameFragment(String lastNameFragment) {
        return this.ownerRepository.findByLastNameContainingIgnoreCase(lastNameFragment)
                .flatMap(o -> this.addPets(Mono.just(o), o.getId()));

    }

    /*
    TODO: Needs optimization
    This approach leads to a repository call for each single owner in the result, this is huge.
    Better approach would be to get all pets in the DB, potentially filtered by the given ownerUUID(s)
    and then add them programmatically to the right owner
     */
    private Mono<Owner> addPets(Mono<Owner> ownerMono, UUID ownerId) {
        Mono<List<Pet>> petListMono = this.petService.findByOwnerId(ownerId).collectList();
        return Mono.zip(ownerMono, petListMono, (o, p) -> { o.setPets(p); return o;});
    }
}
