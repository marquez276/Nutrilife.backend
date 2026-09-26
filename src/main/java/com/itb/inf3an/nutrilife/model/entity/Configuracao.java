package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Configuração chave/valor do sistema (ex.: diasInatividade). */
@Entity
@Table(name = "Configuracao")
@Getter @Setter @NoArgsConstructor
public class Configuracao {

    @Id
    @Column(length = 50)
    private String chave;

    @Column(length = 255, nullable = false)
    private String valor;
}
