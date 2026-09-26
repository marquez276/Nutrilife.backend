package com.itb.inf3an.nutrilife.security.config;

import com.itb.inf3an.nutrilife.model.entity.Usuario;
import com.itb.inf3an.nutrilife.model.enums.TipoUsuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Autorização por dono do recurso: usada em @PreAuthorize("@authz.self(#id)") e nos controllers. */
@Component("authz")
public class Authz {

    private Usuario atual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof Usuario u ? u : null;
    }

    /** true se o usuário autenticado é ADMIN ou é o dono (id) do recurso. Aceita Long, Integer ou String. */
    public boolean self(Object id) {
        Usuario u = atual();
        if (u == null) return false;
        return u.getTipoUsuario() == TipoUsuario.ADMIN
                || (id != null && String.valueOf(u.getId()).equals(String.valueOf(id)));
    }

    /** true se o usuário autenticado é ADMIN ou é dono de pelo menos um dos ids informados. */
    public boolean any(Object... ids) {
        for (Object id : ids) if (id != null && self(id)) return true;
        return false;
    }

    public void checkAny(Object... ids) {
        if (!any(ids)) throw new AccessDeniedException("Recurso pertence a outro usuário");
    }

    /** Versão imperativa para recursos cujo dono só é conhecido depois de carregar o registro. */
    public void check(Long donoId) {
        if (!self(donoId)) throw new AccessDeniedException("Recurso pertence a outro usuário");
    }
}
