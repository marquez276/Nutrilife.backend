package com.itb.inf3an.nutrilife.controller;

import com.itb.inf3an.nutrilife.model.entity.Consulta;
import com.itb.inf3an.nutrilife.repository.ConsultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class ConsultaController {

    @Autowired
    private ConsultaRepository consultaRepo;

    private Map<String, Object> toMap(Consulta c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("date", c.getDataConsulta() != null ? c.getDataConsulta().toString() : null);
        m.put("time", c.getHorario() != null ? c.getHorario().toString().substring(0, 5) : null);
        m.put("videoLink", c.getVideoLink());
        m.put("observations", c.getObservations());
        m.put("paciente", c.getPacienteNome());
        m.put("nutritionist", c.getNutricionistaNome());
        m.put("clienteId", c.getCliente() != null ? c.getCliente().getId() : null);
        m.put("nutricionistaId", c.getNutricionista() != null ? c.getNutricionista().getId() : null);
        m.put("status", c.isCodStatus() ? "confirmed" : "pending");
        return m;
    }

    @GetMapping("/consultas/cliente/{clienteId}")
    public ResponseEntity<?> porCliente(@PathVariable Long clienteId) {
        List<Map<String, Object>> result = new ArrayList<>();
        consultaRepo.findByClienteId(clienteId).forEach(c -> result.add(toMap(c)));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/consultas/nutricionista/{nutricionistaId}")
    public ResponseEntity<?> porNutricionista(@PathVariable Long nutricionistaId) {
        List<Map<String, Object>> result = new ArrayList<>();
        consultaRepo.findByNutricionistaId(nutricionistaId).forEach(c -> result.add(toMap(c)));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/consultas")
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        Consulta c = new Consulta();
        preencherConsulta(c, body);
        c.setCodStatus(true);
        return ResponseEntity.ok(toMap(consultaRepo.save(c)));
    }

    @PutMapping("/consultas/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return consultaRepo.findById(id).map(c -> {
            preencherConsulta(c, body);
            return ResponseEntity.ok(toMap(consultaRepo.save(c)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/consultas/{id}")
    public ResponseEntity<?> remover(@PathVariable Long id) {
        if (!consultaRepo.existsById(id)) return ResponseEntity.notFound().build();
        consultaRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Consulta removida."));
    }

    private void preencherConsulta(Consulta c, Map<String, Object> body) {
        if (body.containsKey("date") && body.get("date") != null)
            c.setDataConsulta(LocalDate.parse(body.get("date").toString()));
        if (body.containsKey("time") && body.get("time") != null)
            c.setHorario(LocalTime.parse(body.get("time").toString()));
        if (body.containsKey("videoLink"))
            c.setVideoLink(body.get("videoLink") != null ? body.get("videoLink").toString() : null);
        if (body.containsKey("observations"))
            c.setObservations(body.get("observations") != null ? body.get("observations").toString() : null);
        if (body.containsKey("paciente"))
            c.setPacienteNome(body.get("paciente") != null ? body.get("paciente").toString() : null);
        if (body.containsKey("nutritionist"))
            c.setNutricionistaNome(body.get("nutritionist") != null ? body.get("nutritionist").toString() : null);

        // vínculos opcionais por ID
        if (body.containsKey("clienteId") && body.get("clienteId") != null) {
            var cli = new com.itb.inf3an.nutrilife.model.entity.Cliente();
            cli.setId(Long.parseLong(body.get("clienteId").toString()));
            c.setCliente(cli);
        }
        if (body.containsKey("nutricionistaId") && body.get("nutricionistaId") != null) {
            var nutri = new com.itb.inf3an.nutrilife.model.entity.Nutricionista();
            nutri.setId(Long.parseLong(body.get("nutricionistaId").toString()));
            c.setNutricionista(nutri);
        }
    }
}
