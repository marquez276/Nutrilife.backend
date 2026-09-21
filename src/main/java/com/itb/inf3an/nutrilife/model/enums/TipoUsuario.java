package com.itb.inf3an.nutrilife.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.itb.inf3an.nutrilife.model.enums.Permission.*;

@Getter
@AllArgsConstructor
public enum TipoUsuario {

    ADMIN(
            Set.of(
                    ADMIN_READ,
                    ADMIN_CREATE,
                    ADMIN_UPDATE,
                    ADMIN_DELETE,
                    ADMIN_MANAGE,
                    NUTRICIONISTA_READ,
                    NUTRICIONISTA_CREATE,
                    NUTRICIONISTA_UPDATE,
                    NUTRICIONISTA_DELETE,
                    NUTRICIONISTA_LIST,
                    NUTRICIONISTA_MANAGE,
                    CLIENTE_READ,
                    CLIENTE_CREATE,
                    CLIENTE_UPDATE,
                    CLIENTE_DELETE,
                    CLIENTE_LIST,
                    CLIENTE_MANAGE,
                    CONSULTA_READ,
                    CONSULTA_UPDATE,
                    CONSULTA_CREATE,
                    CONSULTA_DELETE,
                    CONSULTA_LIST,
                    CONSULTA_MANAGE,
                    PLANO_READ,
                    PLANO_UPDATE,
                    PLANO_CREATE,
                    PLANO_DELETE,
                    PLANO_MANAGE,
                    ALIMENTO_READ,
                    ALIMENTO_UPDATE,
                    ALIMENTO_CREATE,
                    ALIMENTO_DELETE,
                    ANAMNESE_READ,
                    ANAMNESE_UPDATE,
                    ANAMNESE_CREATE,
                    PESAGEM_READ,
                    PESAGEM_CREATE,
                    PESAGEM_UPDATE,
                    PESAGEM_DELETE,
                    AVALIACAO_READ,
                    AVALIACAO_CREATE
            )
    ),
    NUTRICIONISTA(
            Set.of(
                    NUTRICIONISTA_READ,
                    NUTRICIONISTA_UPDATE,
                    CLIENTE_READ,
                    CLIENTE_LIST,
                    CONSULTA_READ,
                    CONSULTA_UPDATE,
                    CONSULTA_LIST,
                    PLANO_READ,
                    PLANO_UPDATE,
                    PLANO_CREATE,
                    PLANO_DELETE,
                    PLANO_MANAGE,
                    ALIMENTO_READ,
                    ALIMENTO_CREATE,
                    ANAMNESE_READ,
                    PESAGEM_READ,
                    AVALIACAO_READ
            )
    ),
    CLIENTE(
            Set.of(
                    CLIENTE_READ,
                    CLIENTE_UPDATE,
                    NUTRICIONISTA_READ,
                    NUTRICIONISTA_LIST,
                    CONSULTA_READ,
                    CONSULTA_CREATE,
                    CONSULTA_LIST,
                    PLANO_READ,
                    ALIMENTO_READ,
                    ANAMNESE_READ,
                    ANAMNESE_CREATE,
                    ANAMNESE_UPDATE,
                    PESAGEM_READ,
                    PESAGEM_CREATE,
                    AVALIACAO_READ,
                    AVALIACAO_CREATE
            )
    );

    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
