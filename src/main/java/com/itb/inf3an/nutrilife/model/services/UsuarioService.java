package com.itb.inf3an.nutrilife.model.services;

import com.itb.inf3an.nutrilife.exceptions.NotFound;
import com.itb.inf3an.nutrilife.model.entity.Usuario;
import com.itb.inf3an.nutrilife.model.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFound("Usuário não encontrado com o email " + email));
    }
}

