package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "Registro_Refeicao")
@Getter
@Setter
public class RegistroRefeicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String meal;
    private String items;
    private Double calories;
    private String time;
    private LocalDate data;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
