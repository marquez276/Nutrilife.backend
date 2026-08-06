package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("ADMIN")
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
public class Admin extends Usuario {

    @Column(nullable = false)
    private boolean principal = false;

    @Column(nullable = false)
    private boolean aprovado = false;
}
