package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.PlanoAlimentar;
import com.itb.inf3an.nutrilife.model.services.PlanoAlimentarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plano")
public class PlanoAlimentarController {

    private static final Logger log = LoggerFactory.getLogger(PlanoAlimentarController.class);

    @Autowired
    private PlanoAlimentarService service;

    @PreAuthorize("@authz.self(#usuarioId)")
    @PostMapping("/gerar/{usuarioId}")
    public PlanoAlimentar gerarPlano(@PathVariable Long usuarioId) {
        log.info("Gerando plano para usuarioId={}", usuarioId);
        PlanoAlimentar plano = service.gerarPlano(usuarioId);
        log.info("Plano id={} retornado para usuarioId={}", plano.getId(), usuarioId);
        return plano;
    }

    @PreAuthorize("@authz.self(#usuarioId)")
    @PostMapping("/regenerar/{usuarioId}")
    public PlanoAlimentar regenerarPlano(@PathVariable Long usuarioId) {
        log.info("Regenerando plano (forçado) para usuarioId={}", usuarioId);
        PlanoAlimentar plano = service.regenerarPlano(usuarioId);
        log.info("Novo plano id={} gerado para usuarioId={}", plano.getId(), usuarioId);
        return plano;
    }
}

