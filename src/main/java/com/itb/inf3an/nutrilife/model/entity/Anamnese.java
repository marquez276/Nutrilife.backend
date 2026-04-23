package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Anamnese")
@Getter
@Setter
public class Anamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @NotNull(message = "Peso é obrigatório")
    @DecimalMin(value = "1.0", message = "Peso deve ser maior que 1kg")
    @DecimalMax(value = "500.0", message = "Peso deve ser menor que 500kg")
    private Double peso;

    @NotNull(message = "Altura é obrigatória")
    @DecimalMin(value = "50.0", message = "Altura deve ser maior que 50cm")
    @DecimalMax(value = "250.0", message = "Altura deve ser menor que 250cm")
    private Double altura;

    @NotNull(message = "Idade é obrigatória")
    @Min(value = 1, message = "Idade deve ser maior que 0")
    @Max(value = 120, message = "Idade deve ser menor que 120")
    private Integer idade;

    @NotBlank(message = "Sexo é obrigatório")
    @Column(length = 20)
    private String sexo;

    @NotBlank(message = "Objetivo é obrigatório")
    @Column(length = 50)
    private String objetivo;

    @Column(length = 50)
    private String atividade;

    @Min(value = 1, message = "Horas de sono deve ser maior que 0")
    @Max(value = 24, message = "Horas de sono deve ser menor que 24")
    private Integer sono;

    @Column(length = 255)
    private String comorbidades;

    @Column(length = 255)
    private String restricoes;
}
