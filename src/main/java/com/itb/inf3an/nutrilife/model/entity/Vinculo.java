package com.itb.inf3an.nutrilife.model.entity;

import com.itb.inf3an.nutrilife.model.enums.OrigemVinculo;
import com.itb.inf3an.nutrilife.model.enums.StatusVinculo;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Relação entre um Cliente e um Nutricionista. O mesmo registro representa tanto a
 * solicitação/convite pendente quanto o vínculo ativo (status muda, a linha é a mesma) —
 * evita sincronizar duas tabelas para a mesma informação, igual PlanoAlimentar já faz com codStatus.
 */
@Entity
@Table(name = "Vinculo")
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Vinculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Nulo até o convite (origem NUTRICIONISTA) ser resgatado por um paciente.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id", nullable = true)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nutricionista", referencedColumnName = "id", nullable = false)
    private Nutricionista nutricionista;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrigemVinculo origem;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StatusVinculo status;

    @Column(name = "codigo_convite", length = 20, unique = true)
    private String codigoConvite;

    @Column(name = "data_criacao", insertable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;
}
