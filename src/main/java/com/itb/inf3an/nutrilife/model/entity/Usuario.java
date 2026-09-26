package com.itb.inf3an.nutrilife.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itb.inf3an.nutrilife.model.enums.TipoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// Propriedades de UserDetails nunca devem ir para o JSON (principalmente o hash da senha)
@JsonIgnoreProperties({"password", "username", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
@Entity
@Table(name = "Usuario")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario implements UserDetails {

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
    @Column(length = 100, nullable = false, unique = true)
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

    // Preenchido pelo DEFAULT do banco
    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    // Aliases para compatibilidade com o padrão da Pizzaria
    public String getNome() {
        return this.nomeCompleto;
    }

    public void setNome(String nome) {
        this.nomeCompleto = nome;
    }

    public void setPassword(String password) {
        this.senha = password;
    }

    // Implementação de UserDetails do Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (tipoUsuario != null) {
            return tipoUsuario.getAuthorities();
        }
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
