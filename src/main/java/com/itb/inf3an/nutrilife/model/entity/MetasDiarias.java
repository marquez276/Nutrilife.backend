package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "Metas_Diarias")
@Getter
@Setter
public class MetasDiarias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "cliente_id", unique = true)
    private Cliente cliente;

    private Integer metaCalorias;
    private Integer tdee;
    private Integer bmr;
    private Double pesoIdeal;
    private Double pesoInicial;
    private Double imc;

    @Column(length = 50)
    private String statusPeso;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
