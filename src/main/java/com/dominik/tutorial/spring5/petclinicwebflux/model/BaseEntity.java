package com.dominik.tutorial.spring5.petclinicwebflux.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Setter
@Getter
public abstract class BaseEntity {
    @Id
    private UUID id;

    public BaseEntity(UUID id) {
        this.id = id;
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public BaseEntity() {
        this.id = UUID.randomUUID();
    }
}
