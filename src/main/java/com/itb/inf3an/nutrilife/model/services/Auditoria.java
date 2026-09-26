package com.itb.inf3an.nutrilife.model.services;

import com.itb.inf3an.nutrilife.model.entity.LogAtividade;
import com.itb.inf3an.nutrilife.model.entity.Usuario;
import com.itb.inf3an.nutrilife.model.repository.LogAtividadeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Grava em Log_Atividade o que acontece no sistema (aparece em Admin > Logs de Atividades). */
@Component
public class Auditoria {

    private final LogAtividadeRepository repo;

    public Auditoria(LogAtividadeRepository repo) {
        this.repo = repo;
    }

    /** Registra como o usuário autenticado da requisição (ou "Sistema" se não houver). */
    public void registrar(String acao) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        registrar(auth != null && auth.getPrincipal() instanceof Usuario u ? u.getNomeCompleto() : "Sistema", acao, "Sucesso");
    }

    public void registrar(String usuarioNome, String acao, String status) {
        LogAtividade l = new LogAtividade();
        l.setUsuarioNome(usuarioNome);
        l.setAcao(acao.length() > 255 ? acao.substring(0, 255) : acao);
        l.setStatus(status);
        repo.save(l);
    }
}
