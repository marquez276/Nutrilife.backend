package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Consulta")
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "data_consulta", nullable = false)
    private LocalDate dataConsulta;

    @Column(nullable = false)
    private LocalTime horario;

    private boolean codStatus;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id")
    private Cliente cliente;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nutricionista", referencedColumnName = "id")
    private Nutricionista nutricionista;

    // campos extras para a agenda
    @Column(length = 500)
    private String videoLink;

    @Column(length = 1000)
    private String observations;

    @Column(length = 100)
    private String pacienteNome;

    @Column(length = 100)
    private String nutricionistaNome;
}
