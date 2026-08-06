package com.itb.inf3an.nutrilife.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Alimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Double calorias;
    private Double proteina;
    private Double carboidrato;
    private Double gordura;

    // flags de restrição — não persistidas no banco, usadas só na geração do plano
    @Transient @JsonIgnore private boolean temLactose;
    @Transient @JsonIgnore private boolean temGluten;
    @Transient @JsonIgnore private boolean temAnimal;
    @Transient @JsonIgnore private boolean temCarne;

    // alimento customizado por paciente (refeicao_id será null)
    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "customizado")
    private boolean customizado;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @ManyToOne
    @JoinColumn(name = "refeicao_id")
    @JsonBackReference("refeicao-alimentos")
    private Refeicao refeicao;
}
