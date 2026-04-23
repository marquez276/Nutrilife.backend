package com.itb.inf3an.nutrilife.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Plano_Alimentar")
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PlanoAlimentar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_final", nullable = true)
    private LocalDate dataFinal;

    @Column(name = "anamnese_hash", length = 255, nullable = true)
    private String anamneseHash;

    private boolean codStatus;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id", nullable = false)
    private Cliente cliente;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nutricionista", referencedColumnName = "id", nullable = true)
    private Nutricionista nutricionista;

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference("plano-refeicoes")
    private List<Refeicao> refeicoes;
}
