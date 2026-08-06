package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Nutricionista_Perfil")
@Getter @Setter
public class NutricionistaPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "nutricionista_id", nullable = false, unique = true)
    private Nutricionista nutricionista;

    @Column(length = 100)
    private String especialidade;

    @Column(length = 1000)
    private String bio;

    @Column(length = 20)
    private String whatsapp;

    @Column(length = 100)
    private String instagram;

    @Column(length = 100)
    private String linkedin;

    @Column(length = 200)
    private String website;

    // JSON serializado: lista de {label, value}
    @Column(length = 2000)
    private String precos;

    // Uma experiência por linha
    @Column(length = 2000)
    private String experiencia;

    // URLs de imagens separadas por newline
    @Lob
    @Column(columnDefinition = "TEXT")
    private String portfolio;

    // URLs/títulos de vídeos separados por newline
    @Column(length = 2000)
    private String videos;
}
