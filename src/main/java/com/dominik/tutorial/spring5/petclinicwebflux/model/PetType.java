package com.dominik.tutorial.spring5.petclinicwebflux.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PetType extends BaseEntity {
    private String typeName;

    @Builder
    public PetType(UUID id, String typeName) {
        super(id);
        this.typeName = typeName;
    }

    public PetType() {
        super();
    }

    @Override
    public String toString() {
        if (this.typeName == null) {
            return "null type";
        }
        return this.typeName;
    }
}
