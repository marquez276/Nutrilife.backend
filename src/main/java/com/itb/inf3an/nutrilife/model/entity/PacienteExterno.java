package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Paciente cadastrado manualmente pelo nutricionista, sem conta/login no NutriLife. */
@Entity
@Table(name = "Paciente_Externo")
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PacienteExterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nutricionista", referencedColumnName = "id", nullable = false)
    private Nutricionista nutricionista;

    @NotBlank(message = "Nome é obrigatório")
    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 50)
    private String objetivo;

    @Column(length = 1000)
    private String observacoes;

    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;
}
