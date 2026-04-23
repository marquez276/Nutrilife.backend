package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("NUTRICIONISTA")
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
public class Nutricionista extends Usuario {

}
