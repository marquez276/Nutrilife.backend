package com.itb.inf3an.nutrilife.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "Refeicao")
@Getter
@Setter
public class Refeicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    // Usado apenas pelo Plano Personalizado (nutricionista define); o plano automático não preenche.
    private LocalTime horario;

    @ManyToOne
    @JoinColumn(name = "plano_id")
    @JsonBackReference("plano-refeicoes")
    private PlanoAlimentar plano;

    @OneToMany(mappedBy = "refeicao", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference("refeicao-alimentos")
    private List<Alimento> alimentos;
}
