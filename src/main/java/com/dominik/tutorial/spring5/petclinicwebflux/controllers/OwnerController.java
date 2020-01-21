package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.exceptions.EntityNotFoundException;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.services.OwnerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Controller
@RequestMapping("/owners")
public class OwnerController extends BaseController {

    public static final String MODEL_ATTRIBUTE_OWNER = "owner";
    private static final String MODEL_ATTRIBUTE_IS_NEW = "isNew";

    private static final String VIEW_CREATE_OR_UPDATE_OWNER_FORM = "owners/createOrUpdateOwnerForm";
    private static final String VIEW_OWNER_DETAILS = "owners/ownerDetails";
    private static final String VIEW_FIND_OWNERS_FORM = "owners/findOwners";
    private static final String VIEW_OWNERS_LIST = "owners/ownersList";


    private final OwnerService ownerService;
    private DataBinder dataBinder;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @InitBinder
    public void avoidIdFieldProcessing(WebDataBinder webDataBinder) {
        webDataBinder.setDisallowedFields("id");
        webDataBinder.setDisallowedFields("pets");
        this.dataBinder = webDataBinder;
    }

    @GetMapping({"", "/"})
    public String findOwners(@RequestParam(required = false) String lastName, Model model) {
        if (lastName == null) {
            return "redirect:/owners/find";
        }
        if (lastName.trim().equals("")) {
            model.addAttribute("selections", this.ownerService.findAll());
        }
        else {
            model.addAttribute("selections", this.ownerService.findByLastNameFragment(lastName));
        }

        return VIEW_OWNERS_LIST;
    }

    @GetMapping("/find")
    public String showFindOwnerForm(Model model) {
        model.addAttribute(MODEL_ATTRIBUTE_OWNER, Mono.just(new Owner()));
        return VIEW_FIND_OWNERS_FORM;
    }

    @GetMapping("/new")
    public String showAddOwnerForm(Model model) {
        model.addAttribute(MODEL_ATTRIBUTE_OWNER, Mono.just(new Owner()));
        model.addAttribute(MODEL_ATTRIBUTE_IS_NEW, true);
        return VIEW_CREATE_OR_UPDATE_OWNER_FORM;
    }

    @PostMapping("/new")
    public Mono<String> createOwner(@ModelAttribute(MODEL_ATTRIBUTE_OWNER) Mono<Owner> owner, Model model) {
        this.dataBinder.validate();
        if (this.dataBinder.getBindingResult().hasErrors()) {
            model.addAttribute(MODEL_ATTRIBUTE_IS_NEW, true);
            return Mono.just(VIEW_CREATE_OR_UPDATE_OWNER_FORM);
        }
        return owner
                .flatMap(this.ownerService::save)
                .flatMap(o -> Mono.just("redirect:/owners/" + o.getId()));
    }

    @GetMapping("/{ownerId}")
    public Mono<String> showOwnerDetails(@PathVariable String ownerId, Model model) {
        UUID ownerUUID = this.fromStringOrThrow(ownerId, Owner.class);
        Mono<Owner> owner = this.ownerService.getById(ownerUUID);

        /*Owner own = owner.block();
        List<Pet> pets = own.getPets().collectList().block();*/

        model.addAttribute(MODEL_ATTRIBUTE_OWNER, owner);
        model.addAttribute(MODEL_ATTRIBUTE_IS_NEW, false);
        return owner
                .flatMap(o -> Mono.just(VIEW_OWNER_DETAILS))
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId)));
    }

    @GetMapping("/{ownerId}/edit")
    public Mono<String> showUpdateOwnerForm(@PathVariable String ownerId, Model model) {
        UUID ownerUUID = this.fromStringOrThrow(ownerId, Owner.class);
        Mono<Owner> owner = this.ownerService.getById(ownerUUID);
        model.addAttribute(MODEL_ATTRIBUTE_OWNER, owner);
        return owner
                .flatMap(o -> Mono.just(VIEW_CREATE_OR_UPDATE_OWNER_FORM))
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId)));
    }

    @PostMapping("/{ownerId}/edit")
    public Mono<String> updateOwner(@PathVariable String ownerId, @ModelAttribute("owner") Mono<Owner> owner, Model model) {
        UUID ownerUUID = this.fromStringOrThrow(ownerId, Owner.class);
        this.dataBinder.validate();
        if (this.dataBinder.getBindingResult().hasErrors()) {
            model.addAttribute(MODEL_ATTRIBUTE_IS_NEW, false);
            return Mono.just(VIEW_CREATE_OR_UPDATE_OWNER_FORM);
        }
        return owner
                .flatMap(o -> {
                    o.setId(ownerUUID);
                    return Mono.just(o);
                })
                .flatMap(this.ownerService::save)
                .flatMap(o -> Mono.just("redirect:/owners/" + ownerId));
    }
}
