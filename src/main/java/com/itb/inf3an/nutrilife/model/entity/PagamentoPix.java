package com.itb.inf3an.nutrilife.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "Pagamento_Pix")
@Getter @Setter
public class PagamentoPix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nutricionista_id", nullable = false)
    private Nutricionista nutricionista;

    @Lob
    @Column(name = "comprovante_url", columnDefinition = "TEXT")
    private String comprovanteUrl;

    // PENDENTE, APROVADO, REJEITADO
    @Column(length = 20, nullable = false)
    private String status = "PENDENTE";

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio = LocalDateTime.now();

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(length = 255)
    private String observacao;
}
