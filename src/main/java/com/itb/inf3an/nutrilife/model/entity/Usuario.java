package com.itb.inf3an.nutrilife.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itb.inf3an.nutrilife.model.enums.TipoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "Usuario")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING)
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(name = "nome_completo", length = 100, nullable = false)
    private String nomeCompleto;

    @Column(length = 15)
    private String cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Column(length = 45, nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String telefone;

    @NotBlank(message = "Senha é obrigatória")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(length = 255, nullable = false)
    private String senha;

    @JsonIgnore
    @Lob
    @Column(nullable = true, columnDefinition = "VARBINARY(MAX)")
    private byte[] imagem;

    private boolean codStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "tipo_usuario", insertable = false, updatable = false)
    private TipoUsuario tipoUsuario;

    @Column(length = 20)
    private String crn;
}
