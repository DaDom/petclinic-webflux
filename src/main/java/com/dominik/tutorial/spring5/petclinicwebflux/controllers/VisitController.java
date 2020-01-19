package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.VisitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Controller
@RequestMapping("/owners/{ownerId}/pets/{petId}/visits/new")
public class VisitController extends BaseController {

    private static final String VIEW_NAME_CREATE_VISIT_FORM = "pets/createOrUpdateVisitForm";

    private static final String MODEL_ATTRIBUTE_OWNER = "owner";
    private static final String MODEL_ATTRIBUTE_PET = "pet";
    private static final String MODEL_ATTRIBUTE_VISIT = "visit";

    private final VisitService visitService;
    private final OwnerService ownerService;
    private final PetService petService;
    private DataBinder dataBinder;

    public VisitController(VisitService visitService, OwnerService ownerService, PetService petService) {
        this.visitService = visitService;
        this.ownerService = ownerService;
        this.petService = petService;
    }

    @InitBinder
    public void disallowIdBinding(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
        this.dataBinder = dataBinder;
    }

    @ModelAttribute(MODEL_ATTRIBUTE_OWNER)
    public Mono<Owner> addOwnerToModel(@PathVariable String ownerId) {
        return this.ownerService.getById(this.fromStringOrThrow(ownerId, Owner.class));
    }

    @ModelAttribute(MODEL_ATTRIBUTE_PET)
    public Mono<Pet> addPetToModel(@PathVariable String ownerId, @PathVariable String petId) {
        return this.petService.findById(
                this.fromStringOrThrow(ownerId, Owner.class),
                this.fromStringOrThrow(petId, Owner.class));
    }

    @GetMapping({"", "/"})
    public Mono<String> showCreateVisitForm(@PathVariable String ownerId, @PathVariable String petId, Model model) {
        UUID ownerUUID = this.fromStringOrThrow(ownerId, Owner.class);
        UUID petUUID = this.fromStringOrThrow(petId, Pet.class);

        return this.ownerService.getById(ownerUUID)
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId)))
                .flatMap(o -> this.petService.findById(ownerUUID, petUUID))
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Pet.class, petId)))
                .flatMap(p -> {
                    model.addAttribute(MODEL_ATTRIBUTE_VISIT, new Visit());
                    return Mono.just(VIEW_NAME_CREATE_VISIT_FORM);
                });
    }

    @PostMapping({"", "/"})
    public Mono<String> createVisit(@PathVariable String ownerId, @PathVariable String petId, Visit visit, Model model) {
        UUID ownerUUID = this.fromStringOrThrow(ownerId, Owner.class);
        UUID petUUID = this.fromStringOrThrow(petId, Pet.class);

        this.dataBinder.validate();
        if (this.dataBinder.getBindingResult().hasErrors()) {
            model.addAttribute(MODEL_ATTRIBUTE_VISIT, visit);
            return Mono.just(VIEW_NAME_CREATE_VISIT_FORM);
        }

        return this.ownerService.getById(ownerUUID)
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId)))
                .flatMap(o -> this.petService.findById(ownerUUID, petUUID))
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Pet.class, petId)))
                .flatMap(p -> this.visitService.createVisit(ownerUUID, petUUID, visit))
                .flatMap(v -> Mono.just("redirect:/owners/" + ownerId));
    }
}
