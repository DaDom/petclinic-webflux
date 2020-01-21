package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetService;
import com.dominik.tutorial.spring5.petclinicwebflux.services.PetTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Controller
@RequestMapping("/owners/{ownerId}/pets")
public class PetController extends BaseController {

    private static final String VIEW_NAME_NEW_PET_FORM = "pets/createOrUpdatePetForm";
    private static final String VIEW_NAME_EDIT_PET_FORM = "pets/createOrUpdatePetForm";

    private static final String MODEL_ATTRIBUTE_OWNER = "owner";
    private static final String MODEL_ATTRIBUTE_PET = "pet";
    private static final String MODEL_ATTRIBUTE_IS_NEW = "isNew";
    private static final String MODEL_ATTRIBUTE_PET_TYPES = "types";

    private final PetService petService;
    private final OwnerService ownerService;
    private final PetTypeService petTypeService;
    private DataBinder dataBinder;

    public PetController(PetService petService, OwnerService ownerService, PetTypeService petTypeService) {
        this.petService = petService;
        this.ownerService = ownerService;
        this.petTypeService = petTypeService;
    }

    @InitBinder({"owner", "pet"})
    public void disallowIdBinding(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
        dataBinder.setDisallowedFields("visits");
        dataBinder.setDisallowedFields("ownerId");
        this.dataBinder = dataBinder;
    }

    @ModelAttribute(MODEL_ATTRIBUTE_OWNER)
    public Mono<Owner> addOwner(@PathVariable String ownerId) {
        UUID ownerUUID = this.fromStringOrThrow(ownerId, Owner.class);
        return this.ownerService.getById(ownerUUID)
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId)));
    }

    @ModelAttribute(MODEL_ATTRIBUTE_PET_TYPES)
    public Flux<String> addPetTypes() {
        return this.petTypeService.findAll();
    }

    @GetMapping("/new")
    public String showNewPetForm(Model model) {
        model.addAttribute(MODEL_ATTRIBUTE_PET, new Pet());
        model.addAttribute(MODEL_ATTRIBUTE_IS_NEW, true);
        return VIEW_NAME_NEW_PET_FORM;
    }

    @PostMapping("/new")
    public Mono<String> createPet(@PathVariable String ownerId, Pet pet, Model model) {
        this.dataBinder.validate();
        if (dataBinder.getBindingResult().hasErrors()) {
            model.addAttribute(MODEL_ATTRIBUTE_IS_NEW, true);
            model.addAttribute(MODEL_ATTRIBUTE_PET, pet);
            return Mono.just(VIEW_NAME_NEW_PET_FORM);
        }
        UUID ownerUUID = this.fromStringOrThrow(ownerId, Owner.class);
        return this.ownerService.getById(ownerUUID)
                .flatMap(o -> this.petService.save(ownerUUID, pet))
                .flatMap(p -> Mono.just("redirect:/owners/" + ownerId.toString()))
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId.toString())));
    }

    @GetMapping("/{petId}/edit")
    public Mono<String> showEditPetForm(@PathVariable String ownerId, @PathVariable String petId, Model model) {
        UUID ownerUUID = this.fromStringOrThrow(ownerId, Owner.class);
        UUID petUUID = this.fromStringOrThrow(petId, Pet.class);
        model.addAttribute(MODEL_ATTRIBUTE_IS_NEW, false);

        return this.ownerService.getById(ownerUUID)
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId)))
                .flatMap(o -> this.petService.findByIdAndOwner(petUUID, ownerUUID))
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Pet.class, petId)))
                .flatMap(p -> {
                    model.addAttribute(MODEL_ATTRIBUTE_PET, p);
                    return Mono.just(p);
                })
                .flatMap(p -> Mono.just(VIEW_NAME_EDIT_PET_FORM));
    }

    @PostMapping("/{petId}/edit")
    public Mono<String> editPet(@PathVariable String ownerId, @PathVariable String petId, Pet pet, Model model) {
        UUID ownerUUID = this.fromStringOrThrow(ownerId, Owner.class);
        UUID petUUID = this.fromStringOrThrow(petId, Pet.class);

        this.dataBinder.validate();
        if (this.dataBinder.getBindingResult().hasErrors()) {
            model.addAttribute(MODEL_ATTRIBUTE_IS_NEW, false);
            model.addAttribute(MODEL_ATTRIBUTE_PET, pet);
            return Mono.just(VIEW_NAME_EDIT_PET_FORM);
        }

        return this.ownerService.getById(ownerUUID)
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId)))
                .flatMap(o -> this.petService.findByIdAndOwner(petUUID, ownerUUID))
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Pet.class, petId)))
                .flatMap(p -> {
                    pet.setId(p.getId());
                    return Mono.just(pet);
                })
                .flatMap(p -> this.petService.save(ownerUUID, p))
                .flatMap(p -> Mono.just("redirect:/owners/" + ownerId));
    }
}
