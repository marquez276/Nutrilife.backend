package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

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
    @Pattern(regexp = "M|F", message = "Sexo deve ser M ou F")
    @Column(length = 20)
    private String sexo;

    @NotBlank(message = "Objetivo é obrigatório")
    @Pattern(regexp = "EMAGRECIMENTO|HIPERTROFIA|MANUTENCAO|PERFORMANCE",
             message = "Objetivo inválido. Use: EMAGRECIMENTO, HIPERTROFIA, MANUTENCAO ou PERFORMANCE")
    @Column(length = 50)
    private String objetivo;

    @Pattern(regexp = "sedentario|leve|moderado|intenso",
             message = "Atividade inválida. Use: sedentario, leve, moderado ou intenso")
    @Column(length = 50)
    private String atividade;

    @Min(value = 1, message = "Horas de sono deve ser maior que 0")
    @Max(value = 24, message = "Horas de sono deve ser menor que 24")
    private Integer sono;

    @Column(length = 255)
    private String comorbidades;

    @Column(length = 255)
    private String restricoes;

    private static final Set<String> COMORBIDADES_VALIDAS = Set.of(
        "Diabetes tipo 1", "Diabetes tipo 2", "Hipertensão arterial", "Colesterol alto",
        "Triglicerídeos altos", "Hipotireoidismo", "Hipertireoidismo", "Obesidade",
        "Síndrome metabólica", "Doença celíaca", "Intolerância à lactose", "Anemia", "Nenhuma"
    );

    private static final Set<String> RESTRICOES_VALIDAS = Set.of(
        "Lactose", "Glúten", "Frutos do mar", "Amendoim", "Ovos", "Soja",
        "Nozes e castanhas", "Vegano", "Vegetariano", "Ovolactovegetariano", "Nenhuma"
    );

    @AssertTrue(message = "Uma ou mais comorbidades informadas são inválidas")
    public boolean isComorbidadesValidas() {
        if (comorbidades == null || comorbidades.isBlank()) return true;
        return Arrays.stream(comorbidades.split(", "))
            .map(String::trim)
            .allMatch(COMORBIDADES_VALIDAS::contains);
    }

    @AssertTrue(message = "Uma ou mais restrições informadas são inválidas")
    public boolean isRestricoesValidas() {
        if (restricoes == null || restricoes.isBlank()) return true;
        return Arrays.stream(restricoes.split(", "))
            .map(String::trim)
            .allMatch(RESTRICOES_VALIDAS::contains);
    }
}
