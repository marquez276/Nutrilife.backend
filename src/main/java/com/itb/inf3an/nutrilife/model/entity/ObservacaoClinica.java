package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Observacao_Clinica")
@Getter
@Setter
public class ObservacaoClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nutricionista_id")
    private Nutricionista nutricionista;

    @Column(length = 2000, nullable = false)
    private String texto;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;
}
