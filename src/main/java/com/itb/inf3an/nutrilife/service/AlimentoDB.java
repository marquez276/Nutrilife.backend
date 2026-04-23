package com.itb.inf3an.nutrilife.service;

import java.util.List;

/**
 * Representa um alimento do banco de dados nutricional.
 * Usado exclusivamente pelo PlanoAlimentarService para gerar refeições.
 */
public class AlimentoDB {

    public final String nome;
    public final double calorias;
    public final double proteina;
    public final double carbo;
    public final double gordura;

    // restrições alimentares
    public final boolean vegetariano;
    public final boolean vegano;
    public final boolean semLactose;
    public final boolean semGluten;

    // índice glicêmico: "baixo", "medio", "alto"
    public final String ig;

    // comorbidades que este alimento é adequado
    public final boolean okDiabetes;
    public final boolean okHipertensao;
    public final boolean okColesterol;

    // refeições em que este alimento pode aparecer
    public final List<String> refeicoes; // "cafe", "almoco", "jantar", "lanche"

    public AlimentoDB(String nome, double cal, double prot, double carbo, double gord,
                      boolean vegetariano, boolean vegano, boolean semLactose, boolean semGluten,
                      String ig, boolean okDiabetes, boolean okHipertensao, boolean okColesterol,
                      List<String> refeicoes) {
        this.nome         = nome;
        this.calorias     = cal;
        this.proteina     = prot;
        this.carbo        = carbo;
        this.gordura      = gord;
        this.vegetariano  = vegetariano;
        this.vegano       = vegano;
        this.semLactose   = semLactose;
        this.semGluten    = semGluten;
        this.ig           = ig;
        this.okDiabetes   = okDiabetes;
        this.okHipertensao = okHipertensao;
        this.okColesterol = okColesterol;
        this.refeicoes    = refeicoes;
    }

    // factory para simplificar criação
    public static AlimentoDB de(String nome, double cal, double prot, double carbo, double gord,
                                 boolean veg, boolean vegano, boolean semLac, boolean semGlut,
                                 String ig, boolean diab, boolean hiper, boolean colest,
                                 String... refeicoes) {
        return new AlimentoDB(nome, cal, prot, carbo, gord, veg, vegano, semLac, semGlut,
                              ig, diab, hiper, colest, List.of(refeicoes));
    }

    public boolean podeNaRefeicao(String tipo) {
        return refeicoes.contains(tipo);
    }
}
