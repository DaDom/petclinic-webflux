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

import javax.validation.Validator;
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
    private final Validator validator;
    private DataBinder dataBinder;

    public OwnerController(OwnerService ownerService, Validator validator) {
        this.ownerService = ownerService;
        this.validator = validator;
    }

    @InitBinder
    public void avoidIdFieldProcessing(WebDataBinder webDataBinder) {
        webDataBinder.setDisallowedFields("id");
        this.dataBinder = webDataBinder;
    }

    @GetMapping({"", "/"})
    public String findOwners(@RequestParam String lastName, Model model) {
        if (lastName == null) {
            return VIEW_FIND_OWNERS_FORM;
        }
        if (lastName.trim().equals("")) {
            model.addAttribute("selections", this.ownerService.findAll());
            return VIEW_OWNERS_LIST;
        }
        return null;
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
        Mono<Owner> owner = this.ownerService.getById(UUID.fromString(ownerId));
        model.addAttribute(MODEL_ATTRIBUTE_OWNER, owner);
        return owner
                .flatMap(o -> Mono.just(VIEW_OWNER_DETAILS))
                .switchIfEmpty(Mono.error(EntityNotFoundException.failedIdLookup(Owner.class, ownerId)));
    }
}
