package com.dominik.tutorial.spring5.petclinicwebflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PetclinicWebfluxApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(PetclinicWebfluxApplication.class);
		app.run(args);
	}

}