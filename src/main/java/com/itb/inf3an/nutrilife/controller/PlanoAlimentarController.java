package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.exceptions.NotFound;
import com.itb.inf3an.nutrilife.model.entity.*;
import com.itb.inf3an.nutrilife.model.enums.OrigemPlano;
import com.itb.inf3an.nutrilife.model.repository.PlanoAlimentarRepository;
import com.itb.inf3an.nutrilife.model.services.PlanoAlimentarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/plano")
public class PlanoAlimentarController {

    private static final Logger log = LoggerFactory.getLogger(PlanoAlimentarController.class);

    @Autowired
    private PlanoAlimentarService service;

    @Autowired
    private PlanoAlimentarRepository planoRepo;

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

    // ── Plano Personalizado (criado manualmente pelo nutricionista vinculado) ──

    @PreAuthorize("@authz.self(#clienteId) or @authz.vinculado(#clienteId)")
    @GetMapping("/personalizado/{clienteId}")
    public PlanoAlimentar buscarPersonalizado(@PathVariable Long clienteId) {
        return planoRepo.findTopByClienteIdAndOrigemAndCodStatusTrue(clienteId, OrigemPlano.PERSONALIZADO)
                .orElseThrow(() -> new NotFound("Este paciente ainda não possui um Plano Personalizado."));
    }

    @PreAuthorize("@authz.vinculado(#clienteId)")
    @PostMapping("/personalizado/{clienteId}")
    public ResponseEntity<PlanoAlimentar> salvarPersonalizado(@PathVariable Long clienteId, @RequestBody Map<String, Object> body) {
        Usuario nutricionistaAtual = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        PlanoAlimentar plano = planoRepo.findTopByClienteIdAndOrigemAndCodStatusTrue(clienteId, OrigemPlano.PERSONALIZADO)
                .orElseGet(() -> {
                    Cliente cliente = new Cliente();
                    cliente.setId(clienteId);
                    PlanoAlimentar novo = new PlanoAlimentar();
                    novo.setCliente(cliente);
                    novo.setOrigem(OrigemPlano.PERSONALIZADO);
                    novo.setCodStatus(true);
                    novo.setRefeicoes(new ArrayList<>());
                    return novo;
                });
        // dataInicio reflete o último envio (não só a criação), para servir de "última atualização" nas telas.
        plano.setDataInicio(LocalDate.now());

        Nutricionista nutri = new Nutricionista();
        nutri.setId(nutricionistaAtual.getId());
        plano.setNutricionista(nutri);

        plano.getRefeicoes().clear();
        for (Map<String, Object> refeicaoBody : castList(body.get("refeicoes"))) {
            Refeicao refeicao = new Refeicao();
            refeicao.setPlano(plano);
            refeicao.setNome((String) refeicaoBody.get("nome"));
            String horario = refeicaoBody.get("horario") != null ? refeicaoBody.get("horario").toString().trim() : "";
            if (!horario.isEmpty()) refeicao.setHorario(LocalTime.parse(horario));
            refeicao.setAlimentos(new ArrayList<>());

            for (Map<String, Object> alimentoBody : castList(refeicaoBody.get("alimentos"))) {
                Alimento alimento = new Alimento();
                alimento.setRefeicao(refeicao);
                alimento.setNome((String) alimentoBody.get("nome"));
                alimento.setCalorias(numeroOuNull(alimentoBody.get("calorias")));
                alimento.setProteina(numeroOuNull(alimentoBody.get("proteina")));
                alimento.setCarboidrato(numeroOuNull(alimentoBody.get("carboidrato")));
                alimento.setGordura(numeroOuNull(alimentoBody.get("gordura")));
                alimento.setQuantidade(numeroOuNull(alimentoBody.get("quantidade")));
                alimento.setUnidade((String) alimentoBody.get("unidade"));
                alimento.setCategoria((String) alimentoBody.get("categoria"));
                refeicao.getAlimentos().add(alimento);
            }
            plano.getRefeicoes().add(refeicao);
        }

        return ResponseEntity.ok(planoRepo.save(plano));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object valor) {
        return valor == null ? List.of() : (List<Map<String, Object>>) valor;
    }

    private Double numeroOuNull(Object valor) {
        if (valor == null) return null;
        String s = valor.toString().trim();
        return s.isEmpty() ? null : Double.valueOf(s);
    }
}

