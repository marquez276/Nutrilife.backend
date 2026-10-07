package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.Nutricionista;
import com.itb.inf3an.nutrilife.model.entity.PacienteExterno;
import com.itb.inf3an.nutrilife.model.repository.PacienteExternoRepository;
import com.itb.inf3an.nutrilife.security.config.Authz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PacienteExternoController {

    @Autowired
    private PacienteExternoRepository repo;

    @Autowired
    private Authz authz;

    @PreAuthorize("@authz.self(#nutricionistaId)")
    @GetMapping("/pacientes-externos/nutricionista/{nutricionistaId}")
    public ResponseEntity<List<PacienteExterno>> listar(@PathVariable Long nutricionistaId) {
        return ResponseEntity.ok(repo.findByNutricionistaId(nutricionistaId));
    }

    @PostMapping("/pacientes-externos")
    public ResponseEntity<PacienteExterno> criar(@RequestBody Map<String, Object> body) {
        Long nutricionistaId = Long.valueOf(String.valueOf(body.get("nutricionistaId")));
        authz.check(nutricionistaId);

        Nutricionista nutri = new Nutricionista();
        nutri.setId(nutricionistaId);

        PacienteExterno p = new PacienteExterno();
        p.setNutricionista(nutri);
        p.setNome(String.valueOf(body.get("nome")));
        p.setObjetivo(body.get("objetivo") != null ? body.get("objetivo").toString() : null);
        p.setObservacoes(body.get("observacoes") != null ? body.get("observacoes").toString() : null);
        return ResponseEntity.ok(repo.save(p));
    }

    @PutMapping("/pacientes-externos/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(p -> {
            authz.check(p.getNutricionista().getId());
            if (body.containsKey("nome")) p.setNome(String.valueOf(body.get("nome")));
            if (body.containsKey("objetivo")) p.setObjetivo(body.get("objetivo") != null ? body.get("objetivo").toString() : null);
            if (body.containsKey("observacoes")) p.setObservacoes(body.get("observacoes") != null ? body.get("observacoes").toString() : null);
            return ResponseEntity.ok(repo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/pacientes-externos/{id}")
    public ResponseEntity<?> remover(@PathVariable Long id) {
        return repo.findById(id).map(p -> {
            authz.check(p.getNutricionista().getId());
            repo.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Paciente removido."));
        }).orElse(ResponseEntity.notFound().build());
    }
}
