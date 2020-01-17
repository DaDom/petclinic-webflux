package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import com.dominik.tutorial.spring5.petclinicwebflux.services.VetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vets.html")
public class VetController extends BaseController {

    private static final String VIEW_NAME_LIST_VETS = "vets/index";

    private final VetService vetService;

    public VetController(VetService vetService) {
        this.vetService = vetService;
    }

    @GetMapping
    public String showAllVets(Model model) {
        model.addAttribute("vets", this.vetService.findAll());
        return VIEW_NAME_LIST_VETS;
    }
}
