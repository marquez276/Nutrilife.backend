package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("NUTRICIONISTA")
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
public class Nutricionista extends Usuario {

    @NotBlank(message = "Especialidade é obrigatória")
    @Column(length = 100)
    private String especialidade;
}
