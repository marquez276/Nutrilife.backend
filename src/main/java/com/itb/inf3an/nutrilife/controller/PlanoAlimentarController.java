package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.PlanoAlimentar;
import com.itb.inf3an.nutrilife.service.PlanoAlimentarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plano")
@CrossOrigin(origins = "*")
public class PlanoAlimentarController {

    private static final Logger log = LoggerFactory.getLogger(PlanoAlimentarController.class);

    @Autowired
    private PlanoAlimentarService service;

    @PostMapping("/gerar/{usuarioId}")
    public PlanoAlimentar gerarPlano(@PathVariable Long usuarioId) {
        log.info("Gerando plano para usuarioId={}", usuarioId);
        PlanoAlimentar plano = service.gerarPlano(usuarioId);
        log.info("Plano id={} retornado para usuarioId={}", plano.getId(), usuarioId);
        return plano;
    }
}
