package com.itb.inf3an.nutrilife.security.token;

import com.itb.inf3an.nutrilife.model.entity.Usuario;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tokens")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    public Long id;
    public String token;
    public boolean revoked;
    public boolean expired;
    @Enumerated(EnumType.STRING)
    public TokenType tokenType = TokenType.BEARER;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    public Usuario usuario;

}

