package com.dominik.tutorial.spring5.petclinicwebflux.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class IndexController {

    @GetMapping({"", "/"})
    public Mono<String> startPage(Model model) {
        model.addAttribute("welcome", Mono.just("Welcome to Petclinic - Webflux version!"));
        return Mono.just("index");
    }
}
