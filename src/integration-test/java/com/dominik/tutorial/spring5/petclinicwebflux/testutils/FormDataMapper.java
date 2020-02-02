package com.dominik.tutorial.spring5.petclinicwebflux.testutils;

import com.dominik.tutorial.spring5.petclinicwebflux.model.Owner;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Pet;
import com.dominik.tutorial.spring5.petclinicwebflux.model.Visit;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.format.DateTimeFormatter;

public class FormDataMapper {

    public static MultiValueMap<String, String> ownerToFormDataMap(Owner owner) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        if (owner.getFirstName() != null) {
            formData.add("firstName", owner.getFirstName());
        }
        if (owner.getLastName() != null) {
            formData.add("lastName", owner.getLastName());
        }
        if (owner.getAddress() != null) {
            formData.add("address", owner.getAddress());
        }
        if (owner.getCity() != null) {
            formData.add("city", owner.getCity());
        }
        if (owner.getTelephone() != null) {
            formData.add("telephone", owner.getTelephone());
        }

        return formData;
    }

    public static MultiValueMap<String, String> petToFormData(Pet pet) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (pet.getId() != null) {
            result.add("id", pet.getId().toString());
        }
        if (pet.getBirthDate() != null) {
            result.add("birthDate", pet.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (pet.getName() != null) {
            result.add("name", pet.getName());
        }
        if (pet.getPetType() != null) {
            result.add("petType", pet.getPetType().toString());
        }

        return result;
    }

    public static MultiValueMap<String, String> visitToFormData(Visit visit) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (visit.getId() != null) {
            result.add("id", visit.getId().toString());
        }
        if (visit.getDate() != null) {
            result.add("date", visit.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (visit.getDescription() != null) {
            result.add("description", visit.getDescription());
        }

        return result;
    }
}
